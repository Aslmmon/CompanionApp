package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.usecase.ScheduleDailyReminderUseCase
import com.aslmmovic.qurancompanion.fakes.FakeJourneyRepository
import com.aslmmovic.qurancompanion.fakes.FakeNotificationScheduler
import com.aslmmovic.qurancompanion.fakes.FakeUserPreferencesRepository
import com.aslmmovic.qurancompanion.fakes.testJourney
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScheduleDailyReminderUseCaseTest {

    @Test
    fun `AC-01 - invoking usecase with default preferences schedules reminder with 15m testing interval`() = runTest {
        val fakeScheduler = FakeNotificationScheduler()
        val fakeJourneyRepo = FakeJourneyRepository().apply {
            todayJourney = testJourney(title = "Abu Bakr Al-Siddiq", subtitle = "The Truthful Companion")
        }
        val fakePrefsRepo = FakeUserPreferencesRepository(
            UserPreferences() // default is reminderHour = 8, reminderMinute = 0, isReminderEnabled = true
        )

        val useCase = ScheduleDailyReminderUseCase(fakeScheduler, fakePrefsRepo, fakeJourneyRepo)

        useCase()

        assertEquals(8, fakeScheduler.scheduledHour)
        assertEquals(0, fakeScheduler.scheduledMinute)
        assertEquals(15L, fakeScheduler.scheduledIntervalMinutes)
        assertEquals("Sahaba Companion: Abu Bakr Al-Siddiq", fakeScheduler.scheduledTitle)
        assertEquals("The Truthful Companion", fakeScheduler.scheduledBody)
        assertFalse(fakeScheduler.isCancelled)
    }

    @Test
    fun `AC-02 - invoking usecase with custom reminder time schedules at updated hour with 15m interval`() = runTest {
        val fakeScheduler = FakeNotificationScheduler()
        val fakeJourneyRepo = FakeJourneyRepository().apply {
            todayJourney = testJourney(title = "Umar ibn Al-Khattab", subtitle = "Al-Faruq")
        }
        val fakePrefsRepo = FakeUserPreferencesRepository(
            UserPreferences(reminderHour = 20, reminderMinute = 0, isReminderEnabled = true)
        )

        val useCase = ScheduleDailyReminderUseCase(fakeScheduler, fakePrefsRepo, fakeJourneyRepo)

        useCase()

        assertEquals(20, fakeScheduler.scheduledHour)
        assertEquals(0, fakeScheduler.scheduledMinute)
        assertEquals(15L, fakeScheduler.scheduledIntervalMinutes)
        assertEquals("Sahaba Companion: Umar ibn Al-Khattab", fakeScheduler.scheduledTitle)
        assertEquals("Al-Faruq", fakeScheduler.scheduledBody)
    }

    @Test
    fun `AC-03 - invoking usecase with disabled preferences cancels existing reminder`() = runTest {
        val fakeScheduler = FakeNotificationScheduler()
        val fakeJourneyRepo = FakeJourneyRepository()
        val fakePrefsRepo = FakeUserPreferencesRepository(
            UserPreferences(isReminderEnabled = false)
        )

        val useCase = ScheduleDailyReminderUseCase(fakeScheduler, fakePrefsRepo, fakeJourneyRepo)

        useCase()

        assertTrue(fakeScheduler.isCancelled)
    }
}
