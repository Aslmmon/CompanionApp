package com.aslmmovic.qurancompanion.presentation.screens.splash

/**
 * One-shot side effects emitted by [SplashViewModel].
 * Consumed by [SplashScreen] to trigger navigation after the splash delay.
 */
sealed class SplashUiEffect {
    data object NavigateNext : SplashUiEffect()
}
