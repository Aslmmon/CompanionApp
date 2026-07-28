package com.aslmmovic.qurancompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.usecase.GetTodayJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.IsJourneyCompletedUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ResetJourneyUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.aslmmovic.qurancompanion.domain.usecase.GetTomorrowJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetWeeklyProgressUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SavePreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetDebugDayOffsetUseCase
import com.aslmmovic.qurancompanion.domain.usecase.IncrementDebugDayOffsetUseCase
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import kotlinx.coroutines.flow.first

import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getTodayJourneyUseCase: GetTodayJourneyUseCase,
    private val getTomorrowJourneyUseCase: GetTomorrowJourneyUseCase,
    private val getWeeklyProgressUseCase: GetWeeklyProgressUseCase,
    private val isJourneyCompletedUseCase: IsJourneyCompletedUseCase,
    private val resetJourneyUseCase: ResetJourneyUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val savePreferencesUseCase: SavePreferencesUseCase,
    private val getDebugDayOffsetUseCase: GetDebugDayOffsetUseCase,
    private val incrementDebugDayOffsetUseCase: IncrementDebugDayOffsetUseCase,
    private val localeProvider: LocaleProvider
) : ViewModel() {

    private val _journey = MutableStateFlow<Journey?>(null)
    val journey: StateFlow<Journey?> = _journey.asStateFlow()

    private val _tomorrowJourney = MutableStateFlow<Journey?>(null)
    val tomorrowJourney: StateFlow<Journey?> = _tomorrowJourney.asStateFlow()

    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    val isCompleted: StateFlow<Boolean> = _journey
        .flatMapLatest { journey ->
            if (journey != null) isJourneyCompletedUseCase(journey.id)
            else flowOf(false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val weeklyProgress: StateFlow<List<Boolean>> = getWeeklyProgressUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), List(7) { false })

    private val _uiEvents = MutableSharedFlow<HomeUiEvent>()
    val uiEvents: SharedFlow<HomeUiEvent> = _uiEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            getUserPreferencesUseCase().collect { prefs ->
                _userPreferences.value = prefs
                _journey.value = getTodayJourneyUseCase()
                _tomorrowJourney.value = getTomorrowJourneyUseCase()
            }
        }

        viewModelScope.launch {
            getDebugDayOffsetUseCase().collect {
                _journey.value = getTodayJourneyUseCase()
                _tomorrowJourney.value = getTomorrowJourneyUseCase()
            }
        }
    }

    fun onBeginJourneyClick() {
        viewModelScope.launch { _uiEvents.emit(HomeUiEvent.NavigateToJourneyFlow) }
    }

    fun onResetCompletionClick() {
        viewModelScope.launch {
            _journey.value?.let { resetJourneyUseCase(it.id) }
        }
    }

    fun onNextJourneyClick() {
        viewModelScope.launch {
            incrementDebugDayOffsetUseCase()
        }
    }

    fun onLanguageSelected(languageCode: String) {
        viewModelScope.launch {
            val current = getUserPreferencesUseCase().first()
            savePreferencesUseCase(current.copy(preferredLanguage = languageCode))
            localeProvider.changeLocale(languageCode)
        }
    }

    fun onToggleTheme(isDarkMode: Boolean) {
        viewModelScope.launch {
            val current = getUserPreferencesUseCase().first()
            savePreferencesUseCase(current.copy(isDarkMode = isDarkMode))
        }
    }
}
