package com.aslmmovic.qurancompanion.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Journey(
    val id: String,
    val dayNumber: Int,
    val title: String,
    val subtitle: String,
    val category: String,
    val person: String?,
    val emotion: String,
    val theme: String,
    val heroQuote: String,
    val intention: String,
    val durationMinutes: Int,
    val difficulty: String,
    val estimatedReadingMinutes: Int,
    val cover: Cover,
    val steps: List<JourneyStep>,
    val references: List<String>,
    val tags: List<String>
)

@Immutable
data class Cover(
    val type: String,
    val asset: String
)

@Immutable
data class JourneyStep(
    val type: StepType,
    val title: String,
    val content: String
)

enum class StepType(val emoji: String, val label: String) {
    INTRO("📖", "INTRO"),
    STORY("📜", "STORY"),
    KEY_LESSONS("💡", "KEY LESSONS"),
    REFLECTION("🤔", "REFLECTION"),
    ACTION("✅", "ACTION"),
    REFERENCES("📚", "REFERENCES")
}
