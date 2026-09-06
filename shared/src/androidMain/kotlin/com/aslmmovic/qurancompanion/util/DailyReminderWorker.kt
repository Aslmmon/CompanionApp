package com.aslmmovic.qurancompanion.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(AndroidNotificationScheduler.EXTRA_TITLE)
            ?: "Sahaba Companion"
        val body = inputData.getString(AndroidNotificationScheduler.EXTRA_BODY)
            ?: "Time for your daily Sahabi discovery."

        Log.d("DailyReminderWorker", "WorkManager executing reminder: title=$title, body=$body")
        AndroidNotificationScheduler(applicationContext).showImmediateNotification(title, body)
        return Result.success()
    }
}
