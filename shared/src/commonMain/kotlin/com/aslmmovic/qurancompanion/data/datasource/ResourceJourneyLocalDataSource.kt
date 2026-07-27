package com.aslmmovic.qurancompanion.data.datasource

import com.aslmmovic.qurancompanion.data.dto.JourneyDto
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import qurancompanion.shared.generated.resources.Res

/**
 * Loads journey data from bundled JSON files (Compose Resources).
 * Caches the last result per locale to avoid redundant file reads.
 */
class ResourceJourneyLocalDataSource(private val json: Json) : JourneyLocalDataSource {

    // Pair of (locale → parsed list) — simple single-entry cache
    private var cache: Pair<String, List<JourneyDto>>? = null

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun loadJourneys(locale: String): List<JourneyDto> {
        cache?.takeIf { it.first == locale }?.let { return it.second }

        val path = if (locale == "ar") "files/ar/journeys.json" else "files/en/journeys.json"
        val result = json.decodeFromString<List<JourneyDto>>(Res.readBytes(path).decodeToString())
        cache = locale to result
        return result
    }
}
