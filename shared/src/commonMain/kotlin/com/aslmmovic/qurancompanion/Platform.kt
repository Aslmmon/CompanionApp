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
expect fun getCurrentDateString(): String

/** Returns the current hour of the day (0-23). Implemented per platform. */
expect fun getCurrentHour(): Int

/** Returns the current minute of the hour (0-59). Implemented per platform. */
expect fun getCurrentMinute(): Int
