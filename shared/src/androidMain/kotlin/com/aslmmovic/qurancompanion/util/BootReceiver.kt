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

/**
 * Handles device boot completion to restore scheduled daily reminders.
 * System broadcasts (BOOT_COMPLETED) require exported="true".
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Boot completed broadcast received. Rescheduling reminders.")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val koin = GlobalContext.getOrNull()
                    val scheduleUseCase = koin?.getOrNull<ScheduleDailyReminderUseCase>()
                    scheduleUseCase?.invoke()
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to reschedule reminders on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
