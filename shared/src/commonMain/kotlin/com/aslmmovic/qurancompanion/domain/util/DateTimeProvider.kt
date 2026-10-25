package com.aslmmovic.qurancompanion.domain.util

interface DateTimeProvider {
    fun getCurrentDayOfYear(): Int
    fun getCurrentDayOfWeek(): Int // 1 = Monday, 7 = Sunday
    fun getCurrentDateString(): String // ISO format: YYYY-MM-DD
    fun getCurrentHour(): Int = 0
    fun getCurrentMinute(): Int = 0
    fun getTimeUntilMidnight(): Pair<Int, Int> = Pair(0, 0)
    fun getCurrentHijriDateString(): String = ""
}

