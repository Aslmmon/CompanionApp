package com.aslmmovic.qurancompanion.domain.repository

import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun observeActiveHabits(): Flow<List<Habit>>
    fun observeCuratedCatalog(): Flow<List<Habit>>
    suspend fun toggleHabitCompletion(habitId: String): Result<Unit>
    suspend fun addHabitFromCatalog(habitId: String): Result<Unit>
    suspend fun createCustomHabit(title: String, description: String, category: HabitCategory): Result<Unit>
    suspend fun removeHabit(habitId: String): Result<Unit>
}
