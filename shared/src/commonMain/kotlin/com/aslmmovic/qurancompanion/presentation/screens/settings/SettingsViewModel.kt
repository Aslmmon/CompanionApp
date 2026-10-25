package com.aslmmovic.qurancompanion.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.domain.model.AppTheme
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.usecase.GetEntitlementsUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.IncrementDebugDayOffsetUseCase
import com.aslmmovic.qurancompanion.domain.usecase.RequestNotificationPermissionUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SavePreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ScheduleDailyReminderUseCase
import com.aslmmovic.qurancompanion.domain.usecase.TriggerImmediateNotificationUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.aslmmovic.qurancompanion.domain.util.AppBuildInfoProvider
import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider
import com.aslmmovic.qurancompanion.domain.util.SystemDateTimeProvider
import com.aslmmovic.qurancompanion.presentation.screens.settings.components.formatReminderTime
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SettingsViewModel(
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val savePreferencesUseCase: SavePreferencesUseCase,
    private val scheduleDailyReminderUseCase: ScheduleDailyReminderUseCase,
    private val requestNotificationPermissionUseCase: RequestNotificationPermissionUseCase,
    private val triggerImmediateNotificationUseCase: TriggerImmediateNotificationUseCase,
    private val incrementDebugDayOffsetUseCase: IncrementDebugDayOffsetUseCase,
    private val localeProvider: LocaleProvider,
    private val appBuildInfoProvider: AppBuildInfoProvider,
    private val getEntitlementsUseCase: GetEntitlementsUseCase,
    private val dateTimeProvider: DateTimeProvider = SystemDateTimeProvider()
) : ViewModel() {

    private val _uiEffects = MutableSharedFlow<SettingsUiEffect>()
    val uiEffects: SharedFlow<SettingsUiEffect> = _uiEffects.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        getUserPreferencesUseCase(),
        getEntitlementsUseCase()
    ) { preferences, entitlements ->
        SettingsUiState(
            userPreferences = preferences,
            isPatron = entitlements.isPatron,
            isSupporter = entitlements.isSupporter,
            isLoading = false,
            isDebug = appBuildInfoProvider.isDebug,
            appVersionName = appBuildInfoProvider.versionName,
            buildNumber = appBuildInfoProvider.buildNumber,
            buildType = appBuildInfoProvider.buildType
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(
                isLoading = true,
                isDebug = appBuildInfoProvider.isDebug,
                appVersionName = appBuildInfoProvider.versionName,
                buildNumber = appBuildInfoProvider.buildNumber,
                buildType = appBuildInfoProvider.buildType
            )
        )

    private val preferencesMutex = Mutex()

    private suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        preferencesMutex.withLock {
            val current = getUserPreferencesUseCase().first()
            savePreferencesUseCase(transform(current))
        }
    }

    fun onToggleReminder(isEnabled: Boolean) {
        viewModelScope.launch {
            if (isEnabled) {
                requestNotificationPermissionUseCase()
            }
            updatePreferences { it.copy(isReminderEnabled = isEnabled) }
            scheduleDailyReminderUseCase()
        }
    }

    fun onUpdateReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            requestNotificationPermissionUseCase()
            updatePreferences {
                it.copy(
                    reminderHour = hour,
                    reminderMinute = minute,
                    isReminderEnabled = true
                )
            }
            scheduleDailyReminderUseCase()

            val currentHour = dateTimeProvider.getCurrentHour()
            val currentMinute = dateTimeProvider.getCurrentMinute()
            val isToday = hour > currentHour || (hour == currentHour && minute > currentMinute)
            val formattedTime = formatReminderTime(hour, minute)
            _uiEffects.emit(SettingsUiEffect.ShowReminderConfirmation(isToday = isToday, formattedTime = formattedTime))
        }
    }

    fun onLanguageSelected(languageCode: String) {
        viewModelScope.launch {
            updatePreferences { it.copy(preferredLanguage = languageCode) }
            localeProvider.changeLocale(languageCode)
        }
    }

    fun onToggleTheme(isDarkMode: Boolean) {
        viewModelScope.launch {
            updatePreferences { it.copy(isDarkMode = isDarkMode) }
        }
    }

    fun onSelectTheme(theme: AppTheme) {
        viewModelScope.launch {
            if (theme.isPremium && !uiState.value.isPatron) {
                _uiEffects.emit(SettingsUiEffect.OpenPaywall)
            } else {
                updatePreferences { it.copy(selectedTheme = theme.id) }
            }
        }
    }

    fun onOpenSupportCard() {
        viewModelScope.launch {
            _uiEffects.emit(SettingsUiEffect.OpenPaywall)
        }
    }

    fun onSimulateNextDay() {
        viewModelScope.launch {
            incrementDebugDayOffsetUseCase()
        }
    }

    fun onTriggerNotification() {
        if (!uiState.value.userPreferences.isReminderEnabled) return
        viewModelScope.launch {
            triggerImmediateNotificationUseCase()
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            _uiEffects.emit(SettingsUiEffect.NavigateBack)
        }
    }
}
