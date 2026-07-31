package com.aslmmovic.qurancompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

sealed class SplashUiEvent {
    data object NavigateNext : SplashUiEvent()
}

class SplashViewModel : ViewModel() {
    private val _uiEvents = MutableSharedFlow<SplashUiEvent>()
    val uiEvents: SharedFlow<SplashUiEvent> = _uiEvents.asSharedFlow()

    init {
        startSplashTimer()
    }

    private fun startSplashTimer() {
        viewModelScope.launch {
            delay(SPLASH_DELAY_MS.milliseconds)
            _uiEvents.emit(SplashUiEvent.NavigateNext)
        }
    }

    companion object {
        private const val SPLASH_DELAY_MS = 5000L
    }
}


