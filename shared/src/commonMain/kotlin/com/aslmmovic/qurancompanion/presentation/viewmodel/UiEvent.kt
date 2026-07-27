package com.aslmmovic.qurancompanion.presentation.viewmodel

/**
 * One-shot side effects emitted by [HomeViewModel].
 * Consumed by the navigation coordinator (App.kt) to drive screen transitions.
 */
sealed class HomeUiEvent {
    data object NavigateToJourneyFlow : HomeUiEvent()
}

/**
 * One-shot side effects emitted by [JourneyViewModel].
 * Consumed by the navigation coordinator (App.kt) to drive screen transitions.
 */
sealed class JourneyUiEvent {
    data object NavigateToCompletion : JourneyUiEvent()
    data object NavigateToHome : JourneyUiEvent()
}
