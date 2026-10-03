package com.aslmmovic.qurancompanion.domain.model

data class Sahaba(
    val id: String,
    val name: String,
    val arabicName: String,
    val initialLetterArabic: String,
    val epithet: String,
    val category: SahabaCategory,
    val journeyCount: Int,
    val bio: String
)
