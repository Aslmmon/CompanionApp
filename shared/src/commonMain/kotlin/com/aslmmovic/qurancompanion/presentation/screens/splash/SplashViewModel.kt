package com.aslmmovic.qurancompanion.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SplashViewModel : ViewModel() {
    private val _uiEffects = MutableSharedFlow<SplashUiEffect>()
    val uiEffects: SharedFlow<SplashUiEffect> = _uiEffects.asSharedFlow()

    init {
        startSplashTimer()
    }

    private fun startSplashTimer() {
        viewModelScope.launch {
            delay(SPLASH_DELAY_MS.milliseconds)
            _uiEffects.emit(SplashUiEffect.NavigateNext)
        }
    }

    companion object {
        private const val SPLASH_DELAY_MS = 5000L
    }
}
