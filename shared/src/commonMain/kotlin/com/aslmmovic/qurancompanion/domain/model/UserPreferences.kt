package com.aslmmovic.qurancompanion.domain.model

data class UserPreferences(
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val isReminderEnabled: Boolean = true,
    val preferredLanguage: String? = null,
    val isDarkMode: Boolean? = null
)
