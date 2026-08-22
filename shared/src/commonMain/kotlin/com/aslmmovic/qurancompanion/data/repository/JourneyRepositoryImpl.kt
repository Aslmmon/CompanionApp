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

    // Cached journey list — loaded ONCE in init, never re-parsed at runtime
    private val _cachedJourneys = MutableStateFlow<List<Journey>>(emptyList())

    // Completion key: "$journeyId|$date"  e.g. "day_003|2026-08-08"
    // Keyed by date so the same journey completed on different days is tracked independently
    private val _completionStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    // Debug offset to manually advance days for testing — starts at 0, loaded in init
    private val _debugDayOffset = MutableStateFlow(0)

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    init {
        scope.launch {
            _debugDayOffset.value = storage.getInt(KEY_DEBUG_DAY_OFFSET, 0)
            // Load and cache the journey list once — avoids repeated JSON parsing
            _cachedJourneys.value = loadJourneysFromSource()
        }
    }

    private suspend fun loadJourneysFromSource(): List<Journey> =
        try {
            localDataSource.loadJourneys(localeProvider.currentLocale).map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }

    override suspend fun getAllJourneys(): List<Journey> = withContext(ioDispatcher) {
        // Return from cache; reload only if empty (e.g. first call before init completes)
        _cachedJourneys.value.ifEmpty {
            val journeys = loadJourneysFromSource()
            _cachedJourneys.value = journeys
            journeys
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

    override fun isCompleted(journeyId: String, date: String): Flow<Boolean> {
        return _completionStates.map { states ->
            // Check in-memory cache first, fall back to persistent storage
            states["$journeyId|$date"]
                ?: storage.getBoolean(completionKey(journeyId, date), false)
        }
    }

    override fun getWeeklyProgress(): Flow<List<Boolean>> {
        // Pure in-memory combine — zero I/O per emission, scales to any library size
        return combine(_cachedJourneys, _completionStates, _debugDayOffset) { journeys, states, offset ->
            if (journeys.isEmpty()) return@combine List(7) { false }

            val todayDayOfYear  = dateTimeProvider.getCurrentDayOfYear()
            val todayDayOfWeek  = dateTimeProvider.getCurrentDayOfWeek() // 1 = Mon, 7 = Sun
            val todayDateString = dateTimeProvider.getCurrentDateString()

            (1..7).map { d ->
                val slotOffset      = d - todayDayOfWeek
                val targetDayOfYear = todayDayOfYear + slotOffset + offset
                val journeyIndex    = ((targetDayOfYear - 1) % journeys.size + journeys.size) % journeys.size
                val journeyId       = journeys[journeyIndex].id
                // Compute the calendar date for this specific week slot
                val slotDate        = offsetDate(todayDateString, slotOffset)
                // Check in-memory first, fall back to storage (handles cold-start)
                states["$journeyId|$slotDate"]
                    ?: storage.getBoolean(completionKey(journeyId, slotDate), false)
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun markCompleted(journeyId: String, date: String) = withContext(ioDispatcher) {
        try {
            storage.putBoolean(completionKey(journeyId, date), true)
            _completionStates.value += ("$journeyId|$date" to true)
        } catch (e: Exception) {
            // Handle exception silently or log
        }
    }

    override suspend fun resetCompletion(journeyId: String, date: String) = withContext(ioDispatcher) {
        try {
            storage.putBoolean(completionKey(journeyId, date), false)
            _completionStates.value += ("$journeyId|$date" to false)
        } catch (e: Exception) {
            // Handle exception silently or log
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
            // Handle exception silently or log
        }
    }

    companion object {
        private const val KEY_DEBUG_DAY_OFFSET = "debug_day_offset"

        /**
         * Storage key for a completion entry.
         * Includes both journeyId and date so the same journey completed on a
         * different calendar day is tracked independently — correct for cycling libraries.
         */
        private fun completionKey(journeyId: String, date: String) =
            "journey_completed_${journeyId}_${date}"

        /**
         * Pure-Kotlin date arithmetic: adds [days] to an ISO date string "YYYY-MM-DD".
         * Uses the Julian Day Number algorithm — no platform imports, safe in commonMain.
         * Correctly handles month/year rollovers in both directions.
         */
        internal fun offsetDate(iso: String, days: Int): String {
            val parts = iso.split("-")
            val y = parts[0].toInt()
            val m = parts[1].toInt()
            val d = parts[2].toInt()
            val jdn = toJulian(y, m, d) + days
            val (ny, nm, nd) = fromJulian(jdn)
            val yStr = ny.toString().padStart(4, '0')
            val mStr = nm.toString().padStart(2, '0')
            val dStr = nd.toString().padStart(2, '0')
            return "$yStr-$mStr-$dStr"
        }

        private fun toJulian(y: Int, m: Int, d: Int): Int {
            val a = (14 - m) / 12
            val yr = y + 4800 - a
            val mo = m + 12 * a - 3
            return d + (153 * mo + 2) / 5 + 365 * yr + yr / 4 - yr / 100 + yr / 400 - 32045
        }

        private fun fromJulian(jdn: Int): Triple<Int, Int, Int> {
            val a  = jdn + 32044
            val b  = (4 * a + 3) / 146097
            val c  = a - 146097 * b / 4
            val d2 = (4 * c + 3) / 1461
            val e  = c - 1461 * d2 / 4
            val mo = (5 * e + 2) / 153
            val day   = e - (153 * mo + 2) / 5 + 1
            val month = mo + 3 - 12 * (mo / 10)
            val year  = 100 * b + d2 - 4800 + mo / 10
            return Triple(year, month, day)
        }
    }
}

