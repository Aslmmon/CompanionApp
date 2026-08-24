package com.aslmmovic.qurancompanion.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val savePreferencesUseCase: SavePreferencesUseCase,
    private val scheduleDailyReminderUseCase: ScheduleDailyReminderUseCase,
    private val requestNotificationPermissionUseCase: RequestNotificationPermissionUseCase,
    private val triggerImmediateNotificationUseCase: TriggerImmediateNotificationUseCase,
    private val incrementDebugDayOffsetUseCase: IncrementDebugDayOffsetUseCase,
    private val localeProvider: LocaleProvider
) : ViewModel() {

    private val _uiEffects = MutableSharedFlow<SettingsUiEffect>()
    val uiEffects: SharedFlow<SettingsUiEffect> = _uiEffects.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = getUserPreferencesUseCase()
        .map { preferences ->
            SettingsUiState(
                userPreferences = preferences,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(isLoading = true)
        )

    private suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        val current = getUserPreferencesUseCase().first()
        savePreferencesUseCase(transform(current))
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
            updatePreferences { it.copy(reminderHour = hour, reminderMinute = minute) }
            scheduleDailyReminderUseCase()
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

    fun onSimulateNextDay() {
        viewModelScope.launch {
            incrementDebugDayOffsetUseCase()
        }
    }

    fun onTriggerNotification() {
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
