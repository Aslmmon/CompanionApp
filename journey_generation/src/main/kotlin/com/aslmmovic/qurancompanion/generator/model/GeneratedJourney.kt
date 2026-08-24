package com.aslmmovic.qurancompanion.generator.model

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedJourney(
    val id: String,
    val dayNumber: Int,
    val title: String,
    val subtitle: String,
    val category: String,
    val person: String? = null,
    val emotion: String,
    val theme: String,
    val heroQuote: String,
    val intention: String,
    val durationMinutes: Int = 10,
    val difficulty: String = "Easy",
    val estimatedReadingMinutes: Int = 8,
    val cover: GeneratedCover,
    val steps: List<GeneratedStep> = emptyList(),
    val references: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

@Serializable
data class GeneratedCover(
    val type: String = "illustration",
    val asset: String
)

@Serializable
data class GeneratedStep(
    val type: String, // Step types: INTRO, STORY, KEY_LESSONS, REFLECTION, ACTION
    val title: String,
    val content: String
)
