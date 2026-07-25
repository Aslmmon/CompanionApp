package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.domain.usecase.GetTodayJourneyUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Android host tests for shared domain logic.
 * These run on the JVM with access to Android resources via Robolectric if needed.
 */
class SharedLogicAndroidHostTest {

    private val repo = FakeJourneyRepository()

    @Test
    fun `GetTodayJourneyUseCase returns correct journey on Android host`() = runTest {
        val expected = testJourney(id = "android-host-test")
        repo.todayJourney = expected

        val result = GetTodayJourneyUseCase(repo)()

        assertEquals(expected, result)
    }

    @Test
    fun `GetTodayJourneyUseCase returns null when no journey configured`() = runTest {
        repo.todayJourney = null
        val result = GetTodayJourneyUseCase(repo)()
        assertNull(result)
    }
}