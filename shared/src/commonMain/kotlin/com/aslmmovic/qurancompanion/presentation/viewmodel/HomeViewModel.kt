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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getTodayJourneyUseCase: GetTodayJourneyUseCase,
    private val getTomorrowJourneyUseCase: GetTomorrowJourneyUseCase,
    private val getWeeklyProgressUseCase: GetWeeklyProgressUseCase,
    private val isJourneyCompletedUseCase: IsJourneyCompletedUseCase,
    private val resetJourneyUseCase: ResetJourneyUseCase
) : ViewModel() {

    private val _journey = MutableStateFlow<Journey?>(null)
    val journey: StateFlow<Journey?> = _journey.asStateFlow()

    private val _tomorrowJourney = MutableStateFlow<Journey?>(null)
    val tomorrowJourney: StateFlow<Journey?> = _tomorrowJourney.asStateFlow()

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
            _journey.value = getTodayJourneyUseCase()
            _tomorrowJourney.value = getTomorrowJourneyUseCase()
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
}
