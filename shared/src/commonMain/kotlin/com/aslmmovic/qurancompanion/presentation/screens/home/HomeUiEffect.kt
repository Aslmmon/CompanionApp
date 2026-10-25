package com.aslmmovic.qurancompanion.presentation.screens.home

import com.aslmmovic.qurancompanion.domain.model.SahabaCategory

/**
 * One-shot side effects emitted by [HomeViewModel].
 * Consumed by [HomeScreen] to drive navigation transitions.
 */
sealed class HomeUiEffect {
    data object NavigateToJourneyFlow : HomeUiEffect()
    data object NavigateToSettings : HomeUiEffect()
    data class NavigateToLibrary(val category: SahabaCategory? = null) : HomeUiEffect()
}
