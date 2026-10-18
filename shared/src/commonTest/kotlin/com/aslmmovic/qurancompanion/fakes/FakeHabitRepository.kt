package com.aslmmovic.qurancompanion.fakes

import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import com.aslmmovic.qurancompanion.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeHabitRepository(
    initialActive: List<Habit> = emptyList(),
    initialCatalog: List<Habit> = emptyList()
) : HabitRepository {

    private val activeFlow = MutableStateFlow(initialActive)
    private val catalogFlow = MutableStateFlow(initialCatalog)

    fun setActiveHabits(habits: List<Habit>) {
        activeFlow.value = habits
    }

    fun setCatalog(habits: List<Habit>) {
        catalogFlow.value = habits
    }

    override fun observeActiveHabits(): Flow<List<Habit>> = activeFlow.asStateFlow()

    override fun observeCuratedCatalog(): Flow<List<Habit>> = catalogFlow.asStateFlow()

    override suspend fun toggleHabitCompletion(habitId: String): Result<Unit> {
        val list = activeFlow.value.toMutableList()
        val index = list.indexOfFirst { it.id == habitId }
        if (index == -1) return Result.failure(NoSuchElementException("Habit $habitId not found"))

        val current = list[index]
        val isDone = !current.isCompletedToday
        val newStreak = if (isDone) current.currentStreak + 1 else maxOf(0, current.currentStreak - 1)
        list[index] = current.copy(
            isCompletedToday = isDone,
            currentStreak = newStreak
        )
        activeFlow.value = list
        return Result.success(Unit)
    }

    override suspend fun addHabitFromCatalog(habitId: String): Result<Unit> {
        val habit = catalogFlow.value.find { it.id == habitId }
            ?: return Result.failure(NoSuchElementException("Catalog habit $habitId not found"))
        if (activeFlow.value.none { it.id == habitId }) {
            activeFlow.value = activeFlow.value + habit
        }
        return Result.success(Unit)
    }

    override suspend fun createCustomHabit(
        title: String,
        description: String,
        category: HabitCategory
    ): Result<Unit> {
        val custom = Habit(
            id = "custom_${title.hashCode()}",
            title = title,
            description = description,
            category = category,
            isCustom = true
        )
        activeFlow.value = activeFlow.value + custom
        return Result.success(Unit)
    }

    override suspend fun removeHabit(habitId: String): Result<Unit> {
        activeFlow.value = activeFlow.value.filter { it.id != habitId }
        return Result.success(Unit)
    }
}
