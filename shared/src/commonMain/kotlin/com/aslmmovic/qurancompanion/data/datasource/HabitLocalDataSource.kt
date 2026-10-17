package com.aslmmovic.qurancompanion.data.datasource

import com.aslmmovic.qurancompanion.data.dto.HabitDto
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import qurancompanion.shared.generated.resources.Res

interface HabitLocalDataSource {
    suspend fun loadHabitsCatalog(locale: String): List<HabitDto>
}

class ResourceHabitLocalDataSource(private val json: Json) : HabitLocalDataSource {

    private val cache = mutableMapOf<String, List<HabitDto>>()

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun loadHabitsCatalog(locale: String): List<HabitDto> {
        val normalizedLocale = if (locale.startsWith("ar", ignoreCase = true)) "ar" else "en"
        cache[normalizedLocale]?.let { return it }

        val path = if (normalizedLocale == "ar") "files/ar/habits.json" else "files/en/habits.json"
        val result = json.decodeFromString<List<HabitDto>>(Res.readBytes(path).decodeToString())
        cache[normalizedLocale] = result
        return result
    }
}
