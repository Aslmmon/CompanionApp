package com.aslmmovic.qurancompanion.domain.util

import com.aslmmovic.qurancompanion.getCurrentDayOfWeek
import com.aslmmovic.qurancompanion.getCurrentDayOfYear

class SystemDateTimeProvider : DateTimeProvider {
    override fun getCurrentDayOfYear(): Int = getCurrentDayOfYear()
    override fun getCurrentDayOfWeek(): Int = getCurrentDayOfWeek()
}
