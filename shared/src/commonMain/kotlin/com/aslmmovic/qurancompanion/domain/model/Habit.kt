package com.aslmmovic.qurancompanion.domain.model

data class Habit(
    val id: String,
    val title: String,
    val description: String,
    val category: HabitCategory,
    val isCustom: Boolean = false,
    val currentStreak: Int = 0,
    val isCompletedToday: Boolean = false,
    val lastCompletedDate: String? = null
)
