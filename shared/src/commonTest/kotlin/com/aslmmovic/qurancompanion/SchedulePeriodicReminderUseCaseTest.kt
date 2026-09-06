package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.usecase.SchedulePeriodicReminderUseCase
import com.aslmmovic.qurancompanion.fakes.FakeJourneyRepository
import com.aslmmovic.qurancompanion.fakes.FakeNotificationScheduler
import com.aslmmovic.qurancompanion.fakes.FakeUserPreferencesRepository
import com.aslmmovic.qurancompanion.fakes.testJourney
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SchedulePeriodicReminderUseCaseTest {

    @Test
    fun `AC-01 - schedules 15-minute periodic reminder with active journey content`() = runTest {
        val fakeScheduler = FakeNotificationScheduler()
        val fakeJourneyRepo = FakeJourneyRepository().apply {
            todayJourney = testJourney(title = "Bilal ibn Rabah", subtitle = "The Mu'adhin of the Prophet")
        }
        val fakePrefsRepo = FakeUserPreferencesRepository(UserPreferences(isReminderEnabled = true))

        val useCase = SchedulePeriodicReminderUseCase(fakeScheduler, fakePrefsRepo, fakeJourneyRepo)
        useCase()

        assertEquals(15L, fakeScheduler.scheduledIntervalMinutes)
        assertEquals("Sahaba Companion: Bilal ibn Rabah", fakeScheduler.scheduledTitle)
        assertEquals("The Mu'adhin of the Prophet", fakeScheduler.scheduledBody)
        assertFalse(fakeScheduler.isCancelled)
    }

    @Test
    fun `AC-02 - cancels periodic reminder when notifications are disabled in preferences`() = runTest {
        val fakeScheduler = FakeNotificationScheduler()
        val fakeJourneyRepo = FakeJourneyRepository()
        val fakePrefsRepo = FakeUserPreferencesRepository(UserPreferences(isReminderEnabled = false))

        val useCase = SchedulePeriodicReminderUseCase(fakeScheduler, fakePrefsRepo, fakeJourneyRepo)
        useCase()

        assertTrue(fakeScheduler.isCancelled)
    }

    @Test
    fun `AC-03 - schedules custom periodic interval when specified`() = runTest {
        val fakeScheduler = FakeNotificationScheduler()
        val fakeJourneyRepo = FakeJourneyRepository().apply {
            todayJourney = testJourney(title = "Uthman ibn Affan", subtitle = "Possessor of the Two Lights")
        }
        val fakePrefsRepo = FakeUserPreferencesRepository(UserPreferences(isReminderEnabled = true))

        val useCase = SchedulePeriodicReminderUseCase(fakeScheduler, fakePrefsRepo, fakeJourneyRepo)
        useCase(intervalMinutes = 30L)

        assertEquals(30L, fakeScheduler.scheduledIntervalMinutes)
        assertEquals("Sahaba Companion: Uthman ibn Affan", fakeScheduler.scheduledTitle)
    }
}
