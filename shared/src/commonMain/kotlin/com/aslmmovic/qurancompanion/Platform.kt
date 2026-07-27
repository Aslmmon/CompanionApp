package com.aslmmovic.qurancompanion

interface Platform {
    val name: String
    val languageCode: String
}

expect fun getPlatform(): Platform

/** Returns the current day of year (1-based). Implemented per platform. */
expect fun getCurrentDayOfYear(): Int

/** Returns the current day of the week (1 = Monday, 7 = Sunday). Implemented per platform. */
expect fun getCurrentDayOfWeek(): Int