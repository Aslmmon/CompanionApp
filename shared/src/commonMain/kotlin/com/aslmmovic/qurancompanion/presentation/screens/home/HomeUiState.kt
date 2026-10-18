package com.aslmmovic.qurancompanion.presentation.screens.home

import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.domain.model.UserPreferences

// Strong Skipping Mode (Kotlin 2.0+): No @Immutable on models holding collections
data class HomeUiState(
    val journey: Journey? = null,
    val tomorrowJourney: Journey? = null,
    val isCompleted: Boolean = false,
    val isBookmarked: Boolean = false,
    val weeklyProgress: List<Boolean> = List(7) { false },
    val streakCount: Int = 0,
    val currentDayOfWeek: Int = 1,
    val hijriDate: String = "",
    val tenPromisedCompanions: List<Sahaba> = emptyList(),
    val todayHabit: Habit? = null,
    val isHabitCompleted: Boolean = false,
    val selectedSahabaDetail: Sahaba? = null,
    val userPreferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false
)
