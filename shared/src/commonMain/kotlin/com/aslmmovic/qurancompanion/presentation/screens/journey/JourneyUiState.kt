package com.aslmmovic.qurancompanion.presentation.screens.journey

import com.aslmmovic.qurancompanion.domain.model.Journey

/**
 * UI State for the Journey Steps Flow screen.
 */
data class JourneyUiState(
    val journey: Journey? = null,
    val currentStepIndex: Int = 0,
    val isBookmarked: Boolean = false,
    val isLoading: Boolean = true
)
