package com.aslmmovic.qurancompanion.data.datasource

import com.aslmmovic.qurancompanion.data.dto.SahabaDto
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import qurancompanion.shared.generated.resources.Res

interface SahabaLocalDataSource {
    suspend fun loadSahabaCatalog(locale: String): List<SahabaDto>
}

class ResourceSahabaLocalDataSource(private val json: Json) : SahabaLocalDataSource {

    private val cache = mutableMapOf<String, List<SahabaDto>>()

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun loadSahabaCatalog(locale: String): List<SahabaDto> {
        val normalizedLocale = if (locale.startsWith("ar", ignoreCase = true)) "ar" else "en"
        cache[normalizedLocale]?.let { return it }

        val path = if (normalizedLocale == "ar") "files/ar/sahaba.json" else "files/en/sahaba.json"
        val result = json.decodeFromString<List<SahabaDto>>(Res.readBytes(path).decodeToString())
        cache[normalizedLocale] = result
        return result
    }
}
