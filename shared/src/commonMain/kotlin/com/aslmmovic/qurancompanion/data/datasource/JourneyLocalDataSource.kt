package com.aslmmovic.qurancompanion.data.datasource

import com.aslmmovic.qurancompanion.data.dto.JourneyDto

/**
 * Abstraction over the raw journey data source (bundled JSON files).
 * Implementations handle loading, parsing, and locale-aware caching.
 */
interface JourneyLocalDataSource {
    suspend fun loadJourneys(locale: String): List<JourneyDto>
}
