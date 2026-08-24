package com.aslmmovic.qurancompanion.presentation.viewmodel

import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.model.UserPreferences

data class AppUiState(
    val isInitialized: Boolean = false,
    val userPreferences: UserPreferences? = null,
    val todayJourney: Journey? = null,
    val isDarkMode: Boolean? = null,
    val isArabic: Boolean = false,
    val startDestination: String? = null
)
