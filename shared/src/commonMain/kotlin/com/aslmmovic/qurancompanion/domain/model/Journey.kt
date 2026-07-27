package com.aslmmovic.qurancompanion.domain.model

data class Journey(
    val id: String,
    val dayNumber: Int,
    val title: String,
    // New field – manually curated subtitle for emotional hook
    val subtitle: String,
    val category: String,
    val durationMinutes: Int,
    val steps: List<JourneyStep>
)

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
