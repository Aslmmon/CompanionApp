package com.aslmmovic.qurancompanion.domain.usecase

import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import com.aslmmovic.qurancompanion.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetActiveHabitsUseCase(
    private val repository: HabitRepository
) {
    operator fun invoke(): Flow<List<Habit>> = repository.observeActiveHabits()
}

class GetCuratedHabitsUseCase(
    private val repository: HabitRepository
) {
    operator fun invoke(
        category: HabitCategory = HabitCategory.ALL,
        searchQuery: String = ""
    ): Flow<List<Habit>> {
        return repository.observeCuratedCatalog().map { catalog ->
            catalog.filter { habit ->
                val matchesCategory = category == HabitCategory.ALL || habit.category == category
                val matchesQuery = searchQuery.isBlank() ||
                    habit.title.contains(searchQuery, ignoreCase = true) ||
                    habit.description.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesQuery
            }
        }
    }
}

class ToggleHabitCompletionUseCase(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: String): Result<Unit> {
        if (habitId.isBlank()) {
            return Result.failure(IllegalArgumentException("Habit ID cannot be blank"))
        }
        return repository.toggleHabitCompletion(habitId)
    }
}

class AddHabitFromCatalogUseCase(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: String): Result<Unit> {
        if (habitId.isBlank()) {
            return Result.failure(IllegalArgumentException("Habit ID cannot be blank"))
        }
        return repository.addHabitFromCatalog(habitId)
    }
}

class CreateCustomHabitUseCase(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        category: HabitCategory
    ): Result<Unit> {
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Title cannot be blank"))
        }
        return repository.createCustomHabit(title.trim(), description.trim(), category)
    }
}

class RemoveHabitUseCase(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: String): Result<Unit> {
        if (habitId.isBlank()) {
            return Result.failure(IllegalArgumentException("Habit ID cannot be blank"))
        }
        return repository.removeHabit(habitId)
    }
}
