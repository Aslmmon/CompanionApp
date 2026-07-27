package com.aslmmovic.qurancompanion.domain.util

class SystemDateTimeProvider : DateTimeProvider {
    override fun getCurrentDayOfYear(): Int = com.aslmmovic.qurancompanion.getCurrentDayOfYear()
    override fun getCurrentDayOfWeek(): Int = com.aslmmovic.qurancompanion.getCurrentDayOfWeek()
}

