package com.aslmmovic.qurancompanion.domain.usecase

import com.aslmmovic.qurancompanion.domain.repository.JourneyRepository
import com.aslmmovic.qurancompanion.domain.repository.UserPreferencesRepository
import com.aslmmovic.qurancompanion.domain.util.NotificationScheduler
import kotlinx.coroutines.flow.first

class ScheduleDailyReminderUseCase(
    private val notificationScheduler: NotificationScheduler,
    private val preferencesRepository: UserPreferencesRepository,
    private val journeyRepository: JourneyRepository
) {
    suspend operator fun invoke(customTitle: String? = null, customBody: String? = null) {
        val preferences = preferencesRepository.getUserPreferences().first()
        if (!preferences.isReminderEnabled) {
            notificationScheduler.cancelDailyReminder()
            return
        }

        val todayJourney = journeyRepository.getTodayJourney()
        val title = customTitle ?: if (todayJourney != null) {
            "Sahaba Companion: ${todayJourney.title}"
        } else {
            "Sahaba Companion"
        }
        val body = customBody ?: (todayJourney?.subtitle ?: "Discover today's journey with the Sahaba.")

        notificationScheduler.scheduleDailyReminder(
            hour = preferences.reminderHour,
            minute = preferences.reminderMinute,
            title = title,
            body = body
        )
    }
}

class RequestNotificationPermissionUseCase(
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(): Boolean {
        return notificationScheduler.requestNotificationPermission()
    }
}

class TriggerImmediateNotificationUseCase(
    private val notificationScheduler: NotificationScheduler,
    private val journeyRepository: JourneyRepository
) {
    suspend operator fun invoke(customTitle: String? = null, customBody: String? = null) {
        notificationScheduler.requestNotificationPermission()
        val todayJourney = journeyRepository.getTodayJourney()
        val title = customTitle ?: if (todayJourney != null) {
            "Sahaba Companion: ${todayJourney.title}"
        } else {
            "Sahaba Companion"
        }
        val body = customBody ?: (todayJourney?.subtitle ?: "Discover today's journey with the Sahaba.")

        notificationScheduler.showImmediateNotification(title = title, body = body)
    }
}

class SchedulePeriodicReminderUseCase(
    private val notificationScheduler: NotificationScheduler,
    private val preferencesRepository: UserPreferencesRepository,
    private val journeyRepository: JourneyRepository
) {
    suspend operator fun invoke(
        intervalMinutes: Long = 15L,
        customTitle: String? = null,
        customBody: String? = null
    ) {
        val preferences = preferencesRepository.getUserPreferences().first()
        if (!preferences.isReminderEnabled) {
            notificationScheduler.cancelDailyReminder()
            return
        }

        val todayJourney = journeyRepository.getTodayJourney()
        val title = customTitle ?: if (todayJourney != null) {
            "Sahaba Companion: ${todayJourney.title}"
        } else {
            "Sahaba Companion"
        }
        val body = customBody ?: (todayJourney?.subtitle ?: "Discover today's journey with the Sahaba.")

        notificationScheduler.schedulePeriodicReminder(
            intervalMinutes = intervalMinutes,
            title = title,
            body = body
        )
    }
}
