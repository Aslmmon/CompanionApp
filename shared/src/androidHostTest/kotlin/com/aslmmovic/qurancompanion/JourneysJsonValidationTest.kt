package com.aslmmovic.qurancompanion

import kotlinx.serialization.json.Json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JourneysJsonValidationTest {
    @Test
    fun validateEnglishAndArabicJourneys() = runTest {
        val json = Json { ignoreUnknownKeys = true }
        
        // Find English JSON file path
        val candidateEnPaths = listOf(
            "src/commonMain/composeResources/files/en/journeys.json",
            "shared/src/commonMain/composeResources/files/en/journeys.json",
            "../shared/src/commonMain/composeResources/files/en/journeys.json"
        )
        val enFile = candidateEnPaths.map { java.io.File(it) }.firstOrNull { it.exists() }
            ?: error("Could not locate en/journeys.json in paths: $candidateEnPaths (cwd: ${java.io.File(".").absolutePath})")
        val enJson = enFile.readText()
        val enJourneys = json.decodeFromString<List<com.aslmmovic.qurancompanion.data.dto.JourneyDto>>(enJson)
        assertEquals(10, enJourneys.size)
        for (journey in enJourneys) {
            assertTrue(journey.id.isNotEmpty())
            assertTrue(journey.title.isNotEmpty())
            assertTrue(journey.subtitle.isNotEmpty() && journey.subtitle != "Placeholder subtitle")
            assertTrue(journey.category.isNotEmpty())
            assertTrue(journey.emotion.isNotEmpty())
            assertTrue(journey.theme.isNotEmpty())
            assertTrue(journey.heroQuote.isNotEmpty())
            assertTrue(journey.intention.isNotEmpty())
            assertTrue(journey.durationMinutes > 0)
            assertTrue(journey.difficulty.isNotEmpty())
            assertTrue(journey.estimatedReadingMinutes > 0)
            assertTrue(journey.cover.type.isNotEmpty())
            assertTrue(journey.cover.asset.isNotEmpty())
            assertTrue(journey.tags.isNotEmpty())
            assertTrue(journey.references.isNotEmpty())
            assertEquals(5, journey.steps.size)
            
            // Check that it contains all 5 required step types
            val stepTypes = journey.steps.map { it.type.name }.toSet()
            val expectedTypes = setOf("INTRO", "STORY", "KEY_LESSONS", "REFLECTION", "ACTION")
            assertEquals(expectedTypes, stepTypes, "Journey ${journey.id} is missing expected step types")
            
            for (step in journey.steps) {
                assertTrue(step.title.isNotEmpty())
                assertTrue(step.content.isNotEmpty())
            }
        }

        // Find Arabic JSON file path
        val candidateArPaths = listOf(
            "src/commonMain/composeResources/files/ar/journeys.json",
            "shared/src/commonMain/composeResources/files/ar/journeys.json",
            "../shared/src/commonMain/composeResources/files/ar/journeys.json"
        )
        val arFile = candidateArPaths.map { java.io.File(it) }.firstOrNull { it.exists() }
            ?: error("Could not locate ar/journeys.json in paths: $candidateArPaths (cwd: ${java.io.File(".").absolutePath})")
        val arJson = arFile.readText()

        val arJourneys = json.decodeFromString<List<com.aslmmovic.qurancompanion.data.dto.JourneyDto>>(arJson)
        assertEquals(5, arJourneys.size)

        for (journey in arJourneys) {
            assertTrue(journey.id.isNotEmpty())
            assertTrue(journey.title.isNotEmpty())
            assertTrue(journey.subtitle.isNotEmpty() && journey.subtitle != "Placeholder subtitle")
            assertTrue(journey.category.isNotEmpty())
            assertTrue(journey.emotion.isNotEmpty())
            assertTrue(journey.theme.isNotEmpty())
            assertTrue(journey.heroQuote.isNotEmpty())
            assertTrue(journey.intention.isNotEmpty())
            assertTrue(journey.durationMinutes > 0)
            assertTrue(journey.difficulty.isNotEmpty())
            assertTrue(journey.estimatedReadingMinutes > 0)
            assertTrue(journey.cover.type.isNotEmpty())
            assertTrue(journey.cover.asset.isNotEmpty())
            assertTrue(journey.tags.isNotEmpty())
            assertTrue(journey.references.isNotEmpty())
            assertEquals(5, journey.steps.size)
            
            val stepTypes = journey.steps.map { it.type.name }.toSet()
            val expectedTypes = setOf("INTRO", "STORY", "KEY_LESSONS", "REFLECTION", "ACTION")
            assertEquals(expectedTypes, stepTypes, "Journey ${journey.id} (Arabic) is missing expected step types")
            
            for (step in journey.steps) {
                assertTrue(step.title.isNotEmpty())
                assertTrue(step.content.isNotEmpty())
            }
        }
    }
}
