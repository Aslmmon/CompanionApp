package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.usecase.ScheduleDailyReminderUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduleDailyReminderUseCaseTest {

    @Test
    fun `invoking usecase with default preferences schedules reminder at 8 AM with journey title`() = runTest {
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
        assertEquals("Sahaba Companion: Abu Bakr Al-Siddiq", fakeScheduler.scheduledTitle)
        assertEquals("The Truthful Companion", fakeScheduler.scheduledBody)
        assertEquals(false, fakeScheduler.isCancelled)
    }

    @Test
    fun `invoking usecase with custom reminder time schedules at updated hour`() = runTest {
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
        assertEquals("Sahaba Companion: Umar ibn Al-Khattab", fakeScheduler.scheduledTitle)
        assertEquals("Al-Faruq", fakeScheduler.scheduledBody)
    }

    @Test
    fun `invoking usecase with disabled preferences cancels existing reminder`() = runTest {
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

class FakeUserPreferencesRepository(
    private var preferences: UserPreferences
) : com.aslmmovic.qurancompanion.domain.repository.UserPreferencesRepository {

    override fun getUserPreferences() = flowOf(preferences)

    override suspend fun saveUserPreferences(preferences: UserPreferences) {
        this.preferences = preferences
    }
}
