package com.aslmmovic.qurancompanion.data.repository

import com.aslmmovic.qurancompanion.data.datasource.JourneyLocalDataSource
import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.data.dto.toDomain
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.repository.JourneyRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider

class JourneyRepositoryImpl(
    private val localDataSource: JourneyLocalDataSource,
    private val localeProvider: LocaleProvider,
    private val storage: KeyValueStorage,
    private val dateTimeProvider: DateTimeProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : JourneyRepository {

    // Keys are journeyIds, values are completion booleans
    private val _completionStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    
    // Debug offset to manually change days/journeys for testing - starts at 0, loaded in init
    private val _debugDayOffset = MutableStateFlow(0)

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    init {
        scope.launch {
            _debugDayOffset.value = storage.getInt(KEY_DEBUG_DAY_OFFSET, 0)
        }
    }

    override suspend fun getAllJourneys(): List<Journey> = withContext(ioDispatcher) {
        try {
            val journeys = localDataSource.loadJourneys(localeProvider.currentLocale).map { it.toDomain() }
            // Seed completion states for all loaded journey IDs
            val states = journeys.associate { journey ->
                journey.id to storage.getBoolean(completionKey(journey.id), false)
            }
            _completionStates.value = states
            journeys
        } catch (e: Exception) {
            // Avoid propagating raw parsing or resource read exceptions
            emptyList()
        }
    }

    override suspend fun getTodayJourney(): Journey? = withContext(ioDispatcher) {
        try {
            val journeys = getAllJourneys()
            if (journeys.isEmpty()) return@withContext null
            val offset = _debugDayOffset.value
            // Cycle through journeys by day-of-year + offset so users see a new journey each day
            val index = (dateTimeProvider.getCurrentDayOfYear() - 1 + offset) % journeys.size
            journeys[index]
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getTomorrowJourney(): Journey? = withContext(ioDispatcher) {
        try {
            val journeys = getAllJourneys()
            if (journeys.isEmpty()) return@withContext null
            val offset = _debugDayOffset.value
            val index = (dateTimeProvider.getCurrentDayOfYear() + offset) % journeys.size
            journeys[index]
        } catch (e: Exception) {
            null
        }
    }

    override fun isCompleted(journeyId: String): Flow<Boolean> {
        return _completionStates.map { it[journeyId] ?: false }
    }

    override fun getWeeklyProgress(): Flow<List<Boolean>> {
        return combine(_completionStates, _debugDayOffset) { states, offset ->
            try {
                val journeys = localDataSource.loadJourneys(localeProvider.currentLocale).map { it.toDomain() }
                if (journeys.isEmpty()) return@combine List(7) { false }

                val todayDayOfYear = dateTimeProvider.getCurrentDayOfYear()
                val todayDayOfWeek = dateTimeProvider.getCurrentDayOfWeek() // 1 = Mon, 7 = Sun

                (1..7).map { d ->
                    val dayOffset = d - todayDayOfWeek
                    val targetDayOfYear = todayDayOfYear + dayOffset + offset
                    val journeyIndex = ((targetDayOfYear - 1) % journeys.size + journeys.size) % journeys.size
                    val journeyId = journeys[journeyIndex].id
                    states[journeyId] ?: false
                }
            } catch (e: Exception) {
                List(7) { false }
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun markCompleted(journeyId: String) = withContext(ioDispatcher) {
        try {
            storage.putBoolean(completionKey(journeyId), true)
            _completionStates.value += (journeyId to true)
        } catch (e: Exception) {
            // handle exception silently or log
        }
    }

    override suspend fun resetCompletion(journeyId: String) = withContext(ioDispatcher) {
        try {
            storage.putBoolean(completionKey(journeyId), false)
            _completionStates.value += (journeyId to false)
        } catch (e: Exception) {
            // handle exception silently or log
        }
    }

    // Debug offset overrides
    override fun getDebugDayOffset(): Flow<Int> = _debugDayOffset

    override suspend fun incrementDebugDayOffset() = withContext(ioDispatcher) {
        try {
            val next = _debugDayOffset.value + 1
            storage.putInt(KEY_DEBUG_DAY_OFFSET, next)
            _debugDayOffset.value = next
        } catch (e: Exception) {
            // handle exception silently or log
        }
    }

    companion object {
        private const val KEY_DEBUG_DAY_OFFSET = "debug_day_offset"
        private fun completionKey(journeyId: String) = "journey_completed_$journeyId"
    }
}
