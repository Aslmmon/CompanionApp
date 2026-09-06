package com.aslmmovic.qurancompanion.fakes

import com.aslmmovic.qurancompanion.domain.util.NotificationScheduler

/**
 * In-memory fake implementation of [NotificationScheduler] for TDD unit tests.
 */
class FakeNotificationScheduler : NotificationScheduler {
    var scheduledHour: Int? = null
    var scheduledMinute: Int? = null
    var scheduledIntervalMinutes: Long? = null
    var scheduledTitle: String? = null
    var scheduledBody: String? = null
    var isCancelled: Boolean = false
    var permissionGranted: Boolean = true
    var immediateNotificationTitle: String? = null
    var immediateNotificationBody: String? = null

    override fun scheduleDailyReminder(hour: Int, minute: Int, title: String, body: String) {
        scheduledHour = hour
        scheduledMinute = minute
        scheduledIntervalMinutes = 15L
        scheduledTitle = title
        scheduledBody = body
        isCancelled = false
    }

    override fun schedulePeriodicReminder(intervalMinutes: Long, title: String, body: String) {
        scheduledIntervalMinutes = intervalMinutes
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
