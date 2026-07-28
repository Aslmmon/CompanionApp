package com.aslmmovic.qurancompanion.data.dto

import com.aslmmovic.qurancompanion.domain.model.Cover
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.model.JourneyStep
import com.aslmmovic.qurancompanion.domain.model.StepType
import kotlinx.serialization.Serializable

@Serializable
data class JourneyDto(
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
    val durationMinutes: Int,
    val difficulty: String,
    val estimatedReadingMinutes: Int,
    val cover: CoverDto,
    val steps: List<JourneyStepDto> = emptyList(),
    val references: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

@Serializable
data class CoverDto(
    val type: String,
    val asset: String
)

@Serializable
data class JourneyStepDto(
    val type: StepTypeDto,
    val title: String,
    val content: String
)

@Serializable
enum class StepTypeDto {
    INTRO, STORY, KEY_LESSONS, REFLECTION, ACTION, REFERENCES
}

// --- Mappers ---

fun JourneyDto.toDomain(): Journey = Journey(
    id = id,
    dayNumber = dayNumber,
    title = title,
    subtitle = subtitle,
    category = category,
    person = person,
    emotion = emotion,
    theme = theme,
    heroQuote = heroQuote,
    intention = intention,
    durationMinutes = durationMinutes,
    difficulty = difficulty,
    estimatedReadingMinutes = estimatedReadingMinutes,
    cover = cover.toDomain(),
    steps = steps.map { it.toDomain() },
    references = references,
    tags = tags
)

fun CoverDto.toDomain(): Cover = Cover(
    type = type,
    asset = asset
)

fun JourneyStepDto.toDomain(): JourneyStep = JourneyStep(
    type = type.toDomain(),
    title = title,
    content = content
)

fun StepTypeDto.toDomain(): StepType = StepType.valueOf(name)
