package com.aslmmovic.qurancompanion.domain.util

interface DateTimeProvider {
    fun getCurrentDayOfYear(): Int
    fun getCurrentDayOfWeek(): Int // 1 = Monday, 7 = Sunday
    fun getCurrentDateString(): String // ISO format: YYYY-MM-DD
}
