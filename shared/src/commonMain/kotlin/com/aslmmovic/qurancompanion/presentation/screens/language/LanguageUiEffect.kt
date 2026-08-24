package com.aslmmovic.qurancompanion.presentation.screens.language

/**
 * One-shot side effects emitted by [LanguageViewModel].
 * Consumed by [LanguageSelectionScreen] to drive navigation.
 */
sealed class LanguageUiEffect {
    data object NavigateToHome : LanguageUiEffect()
}
