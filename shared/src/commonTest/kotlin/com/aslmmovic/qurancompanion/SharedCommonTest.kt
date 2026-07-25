package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.domain.usecase.GetTodayJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.IsJourneyCompletedUseCase
import com.aslmmovic.qurancompanion.domain.usecase.MarkJourneyCompletedUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ResetJourneyUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ---------------------------------------------------------------------------
// GetTodayJourneyUseCase
// ---------------------------------------------------------------------------

class GetTodayJourneyUseCaseTest {

    private val repo = FakeJourneyRepository()
    private val useCase = GetTodayJourneyUseCase(repo)

    @Test
    fun `returns today journey when repository has one`() = runTest {
        val expected = testJourney(id = "day-1")
        repo.todayJourney = expected

        val result = useCase()

        assertEquals(expected, result)
    }

    @Test
    fun `returns null when repository has no journey`() = runTest {
        repo.todayJourney = null

        val result = useCase()

        assertNull(result)
    }
}

// ---------------------------------------------------------------------------
// IsJourneyCompletedUseCase
// ---------------------------------------------------------------------------

class IsJourneyCompletedUseCaseTest {

    private val repo = FakeJourneyRepository()
    private val useCase = IsJourneyCompletedUseCase(repo)

    @Test
    fun `returns false for a journey that has never been marked completed`() = runTest {
        val result = useCase("unknown-id").first()
        assertFalse(result)
    }

    @Test
    fun `returns true after journey is marked completed`() = runTest {
        val journeyId = "day-1"
        repo.markCompleted(journeyId)

        val result = useCase(journeyId).first()

        assertTrue(result)
    }
}

// ---------------------------------------------------------------------------
// MarkJourneyCompletedUseCase
// ---------------------------------------------------------------------------

class MarkJourneyCompletedUseCaseTest {

    private val repo = FakeJourneyRepository()
    private val markCompleted = MarkJourneyCompletedUseCase(repo)
    private val isCompleted = IsJourneyCompletedUseCase(repo)

    @Test
    fun `marks a journey as completed in the repository`() = runTest {
        val journeyId = "day-2"
        assertFalse(isCompleted(journeyId).first())

        markCompleted(journeyId)

        assertTrue(isCompleted(journeyId).first())
    }
}

// ---------------------------------------------------------------------------
// ResetJourneyUseCase
// ---------------------------------------------------------------------------

class ResetJourneyUseCaseTest {

    private val repo = FakeJourneyRepository()
    private val markCompleted = MarkJourneyCompletedUseCase(repo)
    private val resetJourney = ResetJourneyUseCase(repo)
    private val isCompleted = IsJourneyCompletedUseCase(repo)

    @Test
    fun `resets a previously completed journey`() = runTest {
        val journeyId = "day-3"
        markCompleted(journeyId)
        assertTrue(isCompleted(journeyId).first())

        resetJourney(journeyId)

        assertFalse(isCompleted(journeyId).first())
    }

    @Test
    fun `resetting a never-completed journey leaves it as not completed`() = runTest {
        val journeyId = "day-4"
        resetJourney(journeyId)
        assertFalse(isCompleted(journeyId).first())
    }
}