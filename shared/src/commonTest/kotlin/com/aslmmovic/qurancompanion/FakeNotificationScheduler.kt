package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.domain.util.NotificationScheduler

class FakeNotificationScheduler : NotificationScheduler {
    var scheduledHour: Int? = null
    var scheduledMinute: Int? = null
    var scheduledTitle: String? = null
    var scheduledBody: String? = null
    var isCancelled: Boolean = false
    var permissionGranted: Boolean = true
    var immediateNotificationTitle: String? = null
    var immediateNotificationBody: String? = null

    override fun scheduleDailyReminder(hour: Int, minute: Int, title: String, body: String) {
        scheduledHour = hour
        scheduledMinute = minute
        scheduledTitle = title
        scheduledBody = body
        isCancelled = false
    }

    override fun cancelDailyReminder() {
        isCancelled = true
        scheduledHour = null
        scheduledMinute = null
        scheduledTitle = null
        scheduledBody = null
    }

    override suspend fun requestNotificationPermission(): Boolean {
        return permissionGranted
    }

    override fun showImmediateNotification(title: String, body: String) {
        immediateNotificationTitle = title
        immediateNotificationBody = body
    }
}
