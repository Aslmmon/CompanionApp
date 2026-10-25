package com.aslmmovic.qurancompanion.fakes

import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider

/**
 * In-memory fake implementation of [DateTimeProvider] for deterministic unit tests.
 */
class FakeDateTimeProvider(
    var dayOfYear: Int = 1,
    var dayOfWeek: Int = 1,
    var dateString: String = "2026-01-01",
    currentHour: Int = 12,
    currentMinute: Int = 0,
    var remainingTimeUntilMidnight: Pair<Int, Int> = Pair(14, 22),
    var hijriDateString: String = "14 RAMADAN 1447 AH"
) : DateTimeProvider {
    var hour: Int = currentHour
    var minute: Int = currentMinute

    override fun getCurrentDayOfYear(): Int = dayOfYear
    override fun getCurrentDayOfWeek(): Int = dayOfWeek
    override fun getCurrentDateString(): String = dateString
    override fun getCurrentHour(): Int = hour
    override fun getCurrentMinute(): Int = minute
    override fun getTimeUntilMidnight(): Pair<Int, Int> = remainingTimeUntilMidnight
    override fun getCurrentHijriDateString(): String = hijriDateString
}
