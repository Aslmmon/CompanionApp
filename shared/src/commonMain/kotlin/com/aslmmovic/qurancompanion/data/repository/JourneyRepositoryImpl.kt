package com.aslmmovic.qurancompanion.data.repository

import com.aslmmovic.qurancompanion.data.datasource.JourneyLocalDataSource
import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.data.dto.toDomain
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.repository.JourneyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider

class JourneyRepositoryImpl(
    private val localDataSource: JourneyLocalDataSource,
    private val localeProvider: LocaleProvider,
    private val storage: KeyValueStorage,
    private val dateTimeProvider: DateTimeProvider
) : JourneyRepository {

    // Keys are journeyIds, values are completion booleans
    private val _completionStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    override suspend fun getAllJourneys(): List<Journey> {
        val journeys = localDataSource.loadJourneys(localeProvider.currentLocale).map { it.toDomain() }
        // Seed completion states for all loaded journey IDs
        val states = journeys.associate { journey ->
            journey.id to storage.getBoolean(completionKey(journey.id), false)
        }
        _completionStates.value = states
        return journeys
    }

    override suspend fun getTodayJourney(): Journey? {
        val journeys = getAllJourneys()
        if (journeys.isEmpty()) return null
        // Cycle through journeys by day-of-year so users see a new journey each day
        val index = (dateTimeProvider.getCurrentDayOfYear() - 1) % journeys.size
        return journeys[index]
    }

    override suspend fun getTomorrowJourney(): Journey? {
        val journeys = getAllJourneys()
        if (journeys.isEmpty()) return null
        val index = dateTimeProvider.getCurrentDayOfYear() % journeys.size
        return journeys[index]
    }

    override fun isCompleted(journeyId: String): Flow<Boolean> {
        return _completionStates.map { it[journeyId] ?: false }
    }

    override fun getWeeklyProgress(): Flow<List<Boolean>> {
        return _completionStates.map { states ->
            val journeys = localDataSource.loadJourneys(localeProvider.currentLocale).map { it.toDomain() }
            if (journeys.isEmpty()) return@map List(7) { false }

            val todayDayOfYear = dateTimeProvider.getCurrentDayOfYear()
            val todayDayOfWeek = dateTimeProvider.getCurrentDayOfWeek() // 1 = Mon, 7 = Sun

            (1..7).map { d ->
                val dayOffset = d - todayDayOfWeek
                val targetDayOfYear = todayDayOfYear + dayOffset
                val journeyIndex = ((targetDayOfYear - 1) % journeys.size + journeys.size) % journeys.size
                val journeyId = journeys[journeyIndex].id
                states[journeyId] ?: false
            }
        }
    }

    override suspend fun markCompleted(journeyId: String) {
        storage.putBoolean(completionKey(journeyId), true)
        _completionStates.value += (journeyId to true)
    }

    override suspend fun resetCompletion(journeyId: String) {
        storage.putBoolean(completionKey(journeyId), false)
        _completionStates.value += (journeyId to false)
    }

    private fun completionKey(journeyId: String) = "journey_completed_$journeyId"
}
