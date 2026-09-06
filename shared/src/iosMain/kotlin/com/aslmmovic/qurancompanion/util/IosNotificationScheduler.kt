package com.aslmmovic.qurancompanion.util

import com.aslmmovic.qurancompanion.domain.util.NotificationScheduler
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.Foundation.NSDateComponents
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class IosNotificationScheduler : NotificationScheduler {

    companion object {
        private const val NOTIFICATION_IDENTIFIER = "daily_companion_reminder"
    }

    override fun schedulePeriodicReminder(intervalMinutes: Long, title: String, body: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(listOf(NOTIFICATION_IDENTIFIER))

        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = intervalMinutes * 60.0,
            repeats = true
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = NOTIFICATION_IDENTIFIER,
            content = content,
            trigger = trigger
        )

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("Failed to schedule periodic notification: ${error.localizedDescription}")
            }
        }
    }

    override fun scheduleDailyReminder(hour: Int, minute: Int, title: String, body: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(listOf(NOTIFICATION_IDENTIFIER))

        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
        }

        val dateComponents = NSDateComponents().apply {
            setHour(hour.toLong())
            setMinute(minute.toLong())
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents,
            repeats = true
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = NOTIFICATION_IDENTIFIER,
            content = content,
            trigger = trigger
        )

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("Failed to schedule notification: ${error.localizedDescription}")
            }
        }
    }

    override fun cancelDailyReminder() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(listOf(NOTIFICATION_IDENTIFIER))
    }

    override suspend fun requestNotificationPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        center.requestAuthorizationWithOptions(options) { granted, error ->
            continuation.resume(granted && error == null)
        }
    }

    override fun showImmediateNotification(title: String, body: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
        }

        // 1-second delay trigger for instant notification delivery
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = 1.0,
            repeats = false
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = NOTIFICATION_IDENTIFIER + "_immediate",
            content = content,
            trigger = trigger
        )

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("Failed to trigger immediate notification: ${error.localizedDescription}")
            }
        }
    }
}
