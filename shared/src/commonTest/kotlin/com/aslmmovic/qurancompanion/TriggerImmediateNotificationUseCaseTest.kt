package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.domain.usecase.TriggerImmediateNotificationUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TriggerImmediateNotificationUseCaseTest {

    @Test
    fun `invoking usecase with active journey displays notification with journey title and subtitle`() = runTest {
        val fakeScheduler = FakeNotificationScheduler()
        val fakeJourneyRepo = FakeJourneyRepository().apply {
            todayJourney = testJourney(title = "Ali ibn Abi Talib", subtitle = "The Gate of Knowledge")
        }

        val useCase = TriggerImmediateNotificationUseCase(fakeScheduler, fakeJourneyRepo)
        useCase()

        assertEquals("Sahaba Companion: Ali ibn Abi Talib", fakeScheduler.immediateNotificationTitle)
        assertEquals("The Gate of Knowledge", fakeScheduler.immediateNotificationBody)
    }

    @Test
    fun `invoking usecase with no active journey displays fallback notification`() = runTest {
        val fakeScheduler = FakeNotificationScheduler()
        val fakeJourneyRepo = FakeJourneyRepository().apply {
            todayJourney = null
        }

        val useCase = TriggerImmediateNotificationUseCase(fakeScheduler, fakeJourneyRepo)
        useCase()

        assertEquals("Sahaba Companion", fakeScheduler.immediateNotificationTitle)
        assertEquals("Discover today's journey with the Sahaba.", fakeScheduler.immediateNotificationBody)
    }

    @Test
    fun `invoking usecase with custom title and body displays custom notification`() = runTest {
        val fakeScheduler = FakeNotificationScheduler()
        val fakeJourneyRepo = FakeJourneyRepository()

        val useCase = TriggerImmediateNotificationUseCase(fakeScheduler, fakeJourneyRepo)
        useCase(customTitle = "Custom Test Title", customBody = "Custom Test Body")

        assertEquals("Custom Test Title", fakeScheduler.immediateNotificationTitle)
        assertEquals("Custom Test Body", fakeScheduler.immediateNotificationBody)
    }
}
