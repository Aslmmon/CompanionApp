package com.aslmmovic.qurancompanion.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aslmmovic.qurancompanion.domain.usecase.ScheduleDailyReminderUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class DailyReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DAILY_REMINDER = "com.aslmmovic.qurancompanion.ACTION_DAILY_REMINDER"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_BODY = "extra_body"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("DailyReminderReceiver", "onReceive action: $action")

        if (action == ACTION_DAILY_REMINDER) {
            val title = intent.getStringExtra(EXTRA_TITLE) ?: "Sahaba Companion"
            val body = intent.getStringExtra(EXTRA_BODY) ?: "Time for your daily Sahabi discovery."

            // Deliver heads-up notification banner
            AndroidNotificationScheduler(context).showImmediateNotification(title, body)

            // Auto-reschedule for next day (+24h)
            rescheduleReminder(context, intent)
        }
    }

    private fun rescheduleReminder(context: Context, originalIntent: Intent? = null) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val koin = GlobalContext.getOrNull()
                val scheduleUseCase = koin?.getOrNull<ScheduleDailyReminderUseCase>()
                if (scheduleUseCase != null) {
                    scheduleUseCase()
                } else if (originalIntent != null) {
                    val hour = originalIntent.getIntExtra(EXTRA_HOUR, -1)
                    val minute = originalIntent.getIntExtra(EXTRA_MINUTE, -1)
                    val title = originalIntent.getStringExtra(EXTRA_TITLE) ?: "Sahaba Companion"
                    val body = originalIntent.getStringExtra(EXTRA_BODY) ?: "Time for your daily Sahabi discovery."
                    if (hour in 0..23 && minute in 0..59) {
                        AndroidNotificationScheduler(context).scheduleDailyReminder(hour, minute, title, body)
                    }
                }
            } catch (e: Exception) {
                Log.e("DailyReminderReceiver", "Error rescheduling daily reminder", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
