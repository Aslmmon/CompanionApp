package com.aslmmovic.qurancompanion

interface Platform {
    val name: String
    val languageCode: String
    val isDebug: Boolean
}

expect fun getPlatform(): Platform

/** Returns the current day of year (1-based). Implemented per platform. */
expect fun getCurrentDayOfYear(): Int

/** Returns the current day of the week (1 = Monday, 7 = Sunday). Implemented per platform. */
expect fun getCurrentDayOfWeek(): Int

/** Returns the current date as an ISO-8601 string (YYYY-MM-DD). Implemented per platform. */

/** Returns the remaining hours and minutes until midnight as a Pair(hours, minutes). */
expect fun getTimeUntilMidnight(): Pair<Int, Int>

/** Returns the current Hijri date formatted (e.g. "14 RAMADAN 1447 AH"). Implemented per platform. */
expect fun getCurrentHijriDateString(): String

