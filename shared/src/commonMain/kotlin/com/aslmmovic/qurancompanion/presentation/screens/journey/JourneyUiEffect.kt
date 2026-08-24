package com.aslmmovic.qurancompanion.presentation.screens.journey

/**
 * One-shot side effects emitted by [JourneyViewModel].
 * Consumed by [JourneyFlowScreen] and [CompletionScreen] to drive navigation.
 */
sealed class JourneyUiEffect {
    data object NavigateToCompletion : JourneyUiEffect()
    data object NavigateToHome : JourneyUiEffect()
}
