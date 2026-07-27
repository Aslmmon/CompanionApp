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

// ---------------------------------------------------------------------------
// Journeys JSON Content Validation
// ---------------------------------------------------------------------------

class JourneysJsonValidationTest {
    @Test
    fun validateEnglishAndArabicJourneys() = runTest {
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        
        // Find English JSON file path
        var enFile = java.io.File("src/commonMain/composeResources/files/en/journeys.json")
        if (!enFile.exists()) {
            enFile = java.io.File("shared/src/commonMain/composeResources/files/en/journeys.json")
        }
        val enJson = enFile.readText()
        val enJourneys = json.decodeFromString<List<com.aslmmovic.qurancompanion.data.dto.JourneyDto>>(enJson)
        assertEquals(31, enJourneys.size)
        for (journey in enJourneys) {
            assertTrue(journey.id.isNotEmpty())
            assertTrue(journey.title.isNotEmpty())
            assertTrue(journey.subtitle.isNotEmpty() && journey.subtitle != "Placeholder subtitle")
            assertTrue(journey.category.isNotEmpty())
            assertTrue(journey.durationMinutes > 0)
            assertEquals(6, journey.steps.size)
            
            // Check that it contains all 6 required step types
            val stepTypes = journey.steps.map { it.type.name }.toSet()
            val expectedTypes = setOf("INTRO", "STORY", "KEY_LESSONS", "REFLECTION", "ACTION", "REFERENCES")
            assertEquals(expectedTypes, stepTypes, "Journey ${journey.id} is missing expected step types")
            
            for (step in journey.steps) {
                assertTrue(step.title.isNotEmpty())
                assertTrue(step.content.isNotEmpty())
            }
        }

        // Find Arabic JSON file path
        var arFile = java.io.File("src/commonMain/composeResources/files/ar/journeys.json")
        if (!arFile.exists()) {
            arFile = java.io.File("shared/src/commonMain/composeResources/files/ar/journeys.json")
        }
        val arJson = arFile.readText()
        val arJourneys = json.decodeFromString<List<com.aslmmovic.qurancompanion.data.dto.JourneyDto>>(arJson)
        assertEquals(31, arJourneys.size)
        for (journey in arJourneys) {
            assertTrue(journey.id.isNotEmpty())
            assertTrue(journey.title.isNotEmpty())
            assertTrue(journey.subtitle.isNotEmpty() && journey.subtitle != "Placeholder subtitle")
            assertTrue(journey.category.isNotEmpty())
            assertTrue(journey.durationMinutes > 0)
            assertEquals(6, journey.steps.size)
            
            val stepTypes = journey.steps.map { it.type.name }.toSet()
            val expectedTypes = setOf("INTRO", "STORY", "KEY_LESSONS", "REFLECTION", "ACTION", "REFERENCES")
            assertEquals(expectedTypes, stepTypes, "Journey ${journey.id} (Arabic) is missing expected step types")
            
            for (step in journey.steps) {
                assertTrue(step.title.isNotEmpty())
                assertTrue(step.content.isNotEmpty())
            }
        }
    }
}