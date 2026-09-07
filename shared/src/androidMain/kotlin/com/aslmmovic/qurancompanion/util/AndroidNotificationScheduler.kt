package com.aslmmovic.qurancompanion.util

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aslmmovic.qurancompanion.domain.util.NotificationScheduler
import java.util.concurrent.TimeUnit

class AndroidNotificationScheduler(
    private val context: Context
) : NotificationScheduler {

    companion object {
        const val CHANNEL_ID = "daily_reminder_channel"
        const val NOTIFICATION_ID = 1001
        const val REQUEST_CODE_POST_NOTIFICATIONS = 1002
        const val WORK_NAME_DAILY_REMINDER = "daily_companion_reminder_work"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_BODY = "extra_body"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Companion Reminders"
            val descriptionText = "Daily reminders to complete your Sahabi Companion journey"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun schedulePeriodicReminder(intervalMinutes: Long, title: String, body: String) {
        val inputData = Data.Builder()
            .putString(EXTRA_TITLE, title)
            .putString(EXTRA_BODY, body)
            .build()

        // Minimum WorkManager periodic interval is 15 minutes
        val effectiveInterval = intervalMinutes.coerceAtLeast(15L)
        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(effectiveInterval, TimeUnit.MINUTES)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_DAILY_REMINDER,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun scheduleDailyReminder(hour: Int, minute: Int, title: String, body: String) {
        // Scheduled as 15-minute periodic reminder for testing functionality as requested
        schedulePeriodicReminder(15L, title, body)
    }

    override fun cancelDailyReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_DAILY_REMINDER)
    }

    override suspend fun requestNotificationPermission(): Boolean {
        val activity = AndroidActivityProvider.currentActivity
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                if (activity != null) {
                    activity.runOnUiThread {
                        activity.requestPermissions(
                            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                            REQUEST_CODE_POST_NOTIFICATIONS
                        )
                    }
                }
                return false
            }
        } else {
            val areNotificationsEnabled = notificationManager.areNotificationsEnabled()
            if (!areNotificationsEnabled && activity != null) {
                activity.runOnUiThread {
                    showSettingsRedirectDialog(activity)
                }
                return false
            }
        }

        return true
    }

    private fun showSettingsRedirectDialog(activity: Activity) {
        val isArabic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.resources.configuration.locales[0].language == "ar"
        } else {
            @Suppress("DEPRECATION")
            activity.resources.configuration.locale.language == "ar"
        }

        // Show dialog guiding user to system notification settings when OS dialog is permanently dismissed
        AlertDialog.Builder(activity)
            .setTitle(if (isArabic) "تفعيل الإشعارات" else "Enable Notifications")
            .setMessage(
                if (isArabic)
                    "يرجى تفعيل الإشعارات لتلقي تذكير رحلة الصحابي اليومية."
                else
                    "Please enable notifications to receive your daily Sahaba Companion journey reminders."
            )
            .setPositiveButton(if (isArabic) "الإعدادات" else "Settings") { _, _ ->
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                    }
                } else {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", activity.packageName, null)
                    }
                }
                activity.startActivity(intent)
            }
            .setNegativeButton(if (isArabic) "إلغاء" else "Cancel", null)
            .show()
    }

    override fun showImmediateNotification(title: String, body: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to launch host activity when tapping notification banner
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val iconRes = if (context.applicationInfo.icon != 0) {
            context.applicationInfo.icon
        } else {
            android.R.drawable.stat_notify_chat
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(context)
                .setPriority(android.app.Notification.PRIORITY_HIGH)
                .setDefaults(android.app.Notification.DEFAULT_ALL)
        }

        val notification = builder
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .apply {
                if (pendingIntent != null) setContentIntent(pendingIntent)
            }
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
