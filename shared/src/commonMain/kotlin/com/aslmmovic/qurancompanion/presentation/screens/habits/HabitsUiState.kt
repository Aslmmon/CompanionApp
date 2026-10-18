package com.aslmmovic.qurancompanion.presentation.screens.habits

import androidx.compose.runtime.Immutable
import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import org.jetbrains.compose.resources.StringResource

@Immutable
data class HabitsUiState(
    val activeHabits: List<Habit> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progressPercentage: Int = 0,
    val isAllCompleted: Boolean = false,
    val isAddSheetOpen: Boolean = false,
    val catalogHabits: List<Habit> = emptyList(),
    val selectedCatalogCategory: HabitCategory = HabitCategory.ALL,
    val catalogSearchQuery: String = "",
    val selectedHabitForDetail: Habit? = null,
    val isLoading: Boolean = false,
    val errorMessage: StringResource? = null
)
