package com.aslmmovic.qurancompanion.presentation.screens.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.usecase.GetTodayJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.MarkJourneyCompletedUseCase
import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JourneyViewModel(
    private val getTodayJourneyUseCase: GetTodayJourneyUseCase,
    private val markJourneyCompletedUseCase: MarkJourneyCompletedUseCase,
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {

    private val _journey = MutableStateFlow<Journey?>(null)
    val journey: StateFlow<Journey?> = _journey.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    private val _uiEffects = MutableSharedFlow<JourneyUiEffect>()
    val uiEffects: SharedFlow<JourneyUiEffect> = _uiEffects.asSharedFlow()

    init {
        viewModelScope.launch {
            _journey.value = getTodayJourneyUseCase()
        }
    }

    fun onNextStep() {
        val journey = _journey.value ?: return
        val maxIndex = journey.steps.size - 1
        if (_currentStepIndex.value < maxIndex) {
            _currentStepIndex.value++
        }
    }

    fun onPreviousStep() {
        if (_currentStepIndex.value > 0) {
            _currentStepIndex.value--
        }
    }

    fun onFinish() {
        viewModelScope.launch {
            _journey.value?.let { markJourneyCompletedUseCase(it.id, dateTimeProvider.getCurrentDateString()) }
            _uiEffects.emit(JourneyUiEffect.NavigateToCompletion)
        }
    }

    fun onReturnHome() {
        _currentStepIndex.value = 0
        viewModelScope.launch { _uiEffects.emit(JourneyUiEffect.NavigateToHome) }
    }
}
