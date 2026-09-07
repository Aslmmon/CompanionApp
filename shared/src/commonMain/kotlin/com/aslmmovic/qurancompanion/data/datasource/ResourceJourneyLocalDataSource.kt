package com.aslmmovic.qurancompanion.data.datasource

import com.aslmmovic.qurancompanion.data.dto.JourneyDto
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import qurancompanion.shared.generated.resources.Res

/**
 * Loads journey data from bundled JSON files (Compose Resources).
 * Caches parsed results per normalized locale to avoid redundant file reads.
 */
class ResourceJourneyLocalDataSource(private val json: Json) : JourneyLocalDataSource {

    private val cache = mutableMapOf<String, List<JourneyDto>>()

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun loadJourneys(locale: String): List<JourneyDto> {
        val normalizedLocale = if (locale.startsWith("ar", ignoreCase = true)) "ar" else "en"
        cache[normalizedLocale]?.let { return it }

        val path = if (normalizedLocale == "ar") "files/ar/journeys.json" else "files/en/journeys.json"
        val result = json.decodeFromString<List<JourneyDto>>(Res.readBytes(path).decodeToString())
        cache[normalizedLocale] = result
        return result
    }
}
