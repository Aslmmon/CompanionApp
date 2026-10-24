package com.aslmmovic.qurancompanion.presentation.screens.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.usecase.GetBookmarkedJourneyIdsUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetTodayJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.MarkJourneyCompletedUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ToggleJourneyBookmarkUseCase
import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JourneyViewModel(
    private val getTodayJourneyUseCase: GetTodayJourneyUseCase,
    private val markJourneyCompletedUseCase: MarkJourneyCompletedUseCase,
    private val dateTimeProvider: DateTimeProvider,
    private val getBookmarkedJourneyIdsUseCase: GetBookmarkedJourneyIdsUseCase,
    private val toggleJourneyBookmarkUseCase: ToggleJourneyBookmarkUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JourneyUiState())
    val uiState: StateFlow<JourneyUiState> = _uiState.asStateFlow()

    private val _journey = MutableStateFlow<Journey?>(null)
    val journey: StateFlow<Journey?> = _journey.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    private val _uiEffects = MutableSharedFlow<JourneyUiEffect>()
    val uiEffects: SharedFlow<JourneyUiEffect> = _uiEffects.asSharedFlow()

    init {
        viewModelScope.launch {
            val loadedJourney = getTodayJourneyUseCase()
            _journey.value = loadedJourney
            _uiState.update { it.copy(journey = loadedJourney, isLoading = false) }

            if (loadedJourney != null) {
                getBookmarkedJourneyIdsUseCase().collect { bookmarkedIds ->
                    val isBookmarked = bookmarkedIds.contains(loadedJourney.id)
                    _uiState.update { it.copy(isBookmarked = isBookmarked) }
                }
            }
        }
    }

    fun onNextStep() {
        val currentJourney = _uiState.value.journey ?: return
        val maxIndex = currentJourney.steps.size - 1
        if (_uiState.value.currentStepIndex < maxIndex) {
            val newIndex = _uiState.value.currentStepIndex + 1
            _currentStepIndex.value = newIndex
            _uiState.update { it.copy(currentStepIndex = newIndex) }
        }
    }

    fun onPreviousStep() {
        if (_uiState.value.currentStepIndex > 0) {
            val newIndex = _uiState.value.currentStepIndex - 1
            _currentStepIndex.value = newIndex
            _uiState.update { it.copy(currentStepIndex = newIndex) }
        }
    }

    fun onBackPressed() {
        if (_uiState.value.currentStepIndex > 0) {
            val newIndex = _uiState.value.currentStepIndex - 1
            _currentStepIndex.value = newIndex
            _uiState.update { it.copy(currentStepIndex = newIndex) }
        } else {
            viewModelScope.launch {
                _uiEffects.emit(JourneyUiEffect.NavigateToHome)
            }
        }
    }

    fun onToggleBookmark() {
        val currentJourney = _uiState.value.journey ?: return
        viewModelScope.launch {
            val isNowBookmarked = toggleJourneyBookmarkUseCase(currentJourney.id)
            _uiState.update { it.copy(isBookmarked = isNowBookmarked) }
            _uiEffects.emit(JourneyUiEffect.ShowBookmarkFeedback(isNowBookmarked))
        }
    }

    fun onFinish() {
        val currentJourney = _uiState.value.journey ?: return
        viewModelScope.launch {
            markJourneyCompletedUseCase(currentJourney.id, dateTimeProvider.getCurrentDateString())
            _uiEffects.emit(JourneyUiEffect.NavigateToCompletion)
        }
    }

    fun onReturnHome() {
        _currentStepIndex.value = 0
        _uiState.update { it.copy(currentStepIndex = 0) }
        viewModelScope.launch { _uiEffects.emit(JourneyUiEffect.NavigateToHome) }
    }
}
