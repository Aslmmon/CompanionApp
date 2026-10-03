package com.aslmmovic.qurancompanion.data.dto

import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import kotlinx.serialization.Serializable

@Serializable
data class SahabaDto(
    val id: String,
    val name: String,
    val arabicName: String,
    val initialLetterArabic: String,
    val epithet: String,
    val category: String,
    val journeyCount: Int,
    val bio: String
)

fun SahabaDto.toDomain(categoryOverride: SahabaCategory? = null): Sahaba {
    val resolvedCategory = categoryOverride ?: when (category.uppercase().replace("-", "_").replace(" ", "_")) {
        "TEN_PROMISED", "TENPROMISED" -> SahabaCategory.TEN_PROMISED
        "MOTHERS_OF_BELIEVERS", "MOTHERSOFBELIEVERS" -> SahabaCategory.MOTHERS_OF_BELIEVERS
        "ANSAR" -> SahabaCategory.ANSAR
        "MUHAJIRUN" -> SahabaCategory.MUHAJIRUN
        else -> SahabaCategory.ALL
    }
    return Sahaba(
        id = id,
        name = name,
        arabicName = arabicName,
        initialLetterArabic = initialLetterArabic,
        epithet = epithet,
        category = resolvedCategory,
        journeyCount = journeyCount,
        bio = bio
    )
}
