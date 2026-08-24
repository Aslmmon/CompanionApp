package com.aslmmovic.qurancompanion.domain.util

interface NotificationScheduler {
    fun scheduleDailyReminder(hour: Int, minute: Int, title: String, body: String)
    fun cancelDailyReminder()
    suspend fun requestNotificationPermission(): Boolean
    fun showImmediateNotification(title: String, body: String)
}
