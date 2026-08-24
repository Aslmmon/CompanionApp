package com.aslmmovic.qurancompanion.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.usecase.GetDebugDayOffsetUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetTodayJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetTomorrowJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetWeeklyProgressUseCase
import com.aslmmovic.qurancompanion.domain.usecase.IncrementDebugDayOffsetUseCase
import com.aslmmovic.qurancompanion.domain.usecase.IsJourneyCompletedUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ResetJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SavePreferencesUseCase
import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {

    private val _uiEffects = MutableSharedFlow<HomeUiEffect>()
    val uiEffects: SharedFlow<HomeUiEffect> = _uiEffects.asSharedFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        getUserPreferencesUseCase(),
        getDebugDayOffsetUseCase(),
        getWeeklyProgressUseCase()
    ) { preferences, _, weeklyProgress ->
        val today = getTodayJourneyUseCase()
        val tomorrow = getTomorrowJourneyUseCase()
        Triple(preferences, weeklyProgress, Pair(today, tomorrow))
    }.flatMapLatest { (preferences, weeklyProgress, journeys) ->
        val (today, tomorrow) = journeys
        val isCompletedFlow = if (today != null) {
            isJourneyCompletedUseCase(today.id, dateTimeProvider.getCurrentDateString())
        } else {
            flowOf(false)
        }
        isCompletedFlow.map { isCompleted ->
            HomeUiState(
                journey = today,
                tomorrowJourney = tomorrow,
                isCompleted = isCompleted,
                weeklyProgress = weeklyProgress,
                userPreferences = preferences,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )

    private suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        val current = getUserPreferencesUseCase().first()
        savePreferencesUseCase(transform(current))
    }

    fun onBeginJourneyClick() {
        viewModelScope.launch { _uiEffects.emit(HomeUiEffect.NavigateToJourneyFlow) }
    }

    fun onSettingsClick() {
        viewModelScope.launch { _uiEffects.emit(HomeUiEffect.NavigateToSettings) }
    }

    fun onResetCompletionClick() {
        viewModelScope.launch {
            uiState.value.journey?.let {
                resetJourneyUseCase(it.id, dateTimeProvider.getCurrentDateString())
            }
        }
    }

    fun onNextJourneyClick() {
        viewModelScope.launch {
            incrementDebugDayOffsetUseCase()
        }
    }

    fun onToggleTheme(isDarkMode: Boolean) {
        viewModelScope.launch {
            updatePreferences { it.copy(isDarkMode = isDarkMode) }
        }
    }
}
