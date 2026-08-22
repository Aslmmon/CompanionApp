package com.aslmmovic.qurancompanion.presentation.screens.home

import androidx.compose.runtime.Immutable
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.model.UserPreferences

@Immutable
data class HomeUiState(
    val journey: Journey? = null,
    val tomorrowJourney: Journey? = null,
    val isCompleted: Boolean = false,
    val weeklyProgress: List<Boolean> = List(7) { false },
    val userPreferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false
)
