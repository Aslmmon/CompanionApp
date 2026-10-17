package com.aslmmovic.qurancompanion.data.dto

import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import kotlinx.serialization.Serializable

@Serializable
data class HabitDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val isDefaultActive: Boolean = false
)

@Serializable
data class CustomHabitDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String
)

fun HabitDto.toDomain(
    currentStreak: Int = 0,
    isCompletedToday: Boolean = false,
    lastCompletedDate: String? = null
): Habit {
    val domainCategory = when (category.uppercase().replace("-", "_").replace(" ", "_")) {
        "DAILYPRAYERS", "DAILY_PRAYERS" -> HabitCategory.DAILY_PRAYERS
        "ADHKAR" -> HabitCategory.ADHKAR
        "CHARACTER" -> HabitCategory.CHARACTER
        "QURANANDFASTING", "QURAN_AND_FASTING" -> HabitCategory.QURAN_AND_FASTING
        else -> HabitCategory.ALL
    }
    return Habit(
        id = id,
        title = title,
        description = description,
        category = domainCategory,
        isCustom = false,
        currentStreak = currentStreak,
        isCompletedToday = isCompletedToday,
        lastCompletedDate = lastCompletedDate
    )
}
