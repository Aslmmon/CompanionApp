package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.model.JourneyStep
import com.aslmmovic.qurancompanion.domain.model.StepType
import com.aslmmovic.qurancompanion.domain.repository.JourneyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory fake implementation of [JourneyRepository] for unit tests.
 * All state is held in mutable properties so tests can arrange data simply.
 */
class FakeJourneyRepository : JourneyRepository {

    var allJourneys: List<Journey> = emptyList()
    var todayJourney: Journey? = null

    private val completions = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    override suspend fun getAllJourneys(): List<Journey> = allJourneys

    override suspend fun getTodayJourney(): Journey? = todayJourney

    override suspend fun getTomorrowJourney(): Journey? = todayJourney

    override fun isCompleted(journeyId: String): Flow<Boolean> =
        completions.map { it[journeyId] ?: false }

    override fun getWeeklyProgress(): Flow<List<Boolean>> =
        completions.map { completedMap ->
            // Simulating weekly progress by checking completion status for all journeys
            // mapping to the days of the current week (mocking with simple booleans)
            List(7) { index ->
                val journeyId = allJourneys.getOrNull(index)?.id ?: ""
                completedMap[journeyId] ?: false
            }
        }

    override suspend fun markCompleted(journeyId: String) {
        completions.value = completions.value + (journeyId to true)
    }

    override suspend fun resetCompletion(journeyId: String) {
        completions.value = completions.value + (journeyId to false)
    }
}

/** Convenience factory for creating test [Journey] instances. */
fun testJourney(
    id: String = "test-journey-1",
    dayNumber: Int = 1,
    title: String = "Test Journey",
    subtitle: String = "Placeholder subtitle",
    category: String = "Test",
    durationMinutes: Int = 5,
    steps: List<JourneyStep> = listOf(
        JourneyStep(StepType.INTRO, "Intro Title", "Intro content"),
        JourneyStep(StepType.ACTION, "Action Title", "Action content")
    )
) = Journey(id, dayNumber, title, subtitle, category, durationMinutes, steps)
