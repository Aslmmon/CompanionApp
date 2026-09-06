package com.aslmmovic.qurancompanion.fakes

import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.repository.JourneyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory fake implementation of [JourneyRepository] for TDD unit tests.
 * All state is held in mutable properties so tests can arrange data deterministically.
 */
class FakeJourneyRepository : JourneyRepository {

    var allJourneys: List<Journey> = emptyList()
    var todayJourney: Journey? = null
    var tomorrowJourney: Journey? = null

    private val completions = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val debugOffset = MutableStateFlow(0)

    override suspend fun getAllJourneys(): List<Journey> = allJourneys

    override suspend fun getTodayJourney(): Journey? = todayJourney

    override suspend fun getTomorrowJourney(): Journey? = tomorrowJourney ?: todayJourney

    override fun isCompleted(journeyId: String, date: String): Flow<Boolean> =
        completions.map { it["$journeyId|$date"] ?: false }

    override fun getWeeklyProgress(): Flow<List<Boolean>> =
        completions.map { completedMap ->
            List(7) { index ->
                val journeyId = allJourneys.getOrNull(index)?.id ?: ""
                completedMap[journeyId] ?: false
            }
        }

    override suspend fun markCompleted(journeyId: String, date: String) {
        completions.value = completions.value + ("$journeyId|$date" to true)
    }

    override suspend fun resetCompletion(journeyId: String, date: String) {
        completions.value = completions.value + ("$journeyId|$date" to false)
    }

    override fun getDebugDayOffset(): Flow<Int> = debugOffset

    override suspend fun incrementDebugDayOffset() {
        debugOffset.value += 1
    }
}
