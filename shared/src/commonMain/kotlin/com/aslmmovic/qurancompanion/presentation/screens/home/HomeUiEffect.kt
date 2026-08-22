package com.aslmmovic.qurancompanion.presentation.screens.home

/**
 * One-shot side effects emitted by [HomeViewModel].
 * Consumed by [HomeScreen] to drive navigation transitions.
 */
sealed class HomeUiEffect {
    data object NavigateToJourneyFlow : HomeUiEffect()
}
