package com.aslmmovic.qurancompanion.data.dto

import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.model.JourneyStep
import com.aslmmovic.qurancompanion.domain.model.StepType
import kotlinx.serialization.Serializable

@Serializable
data class JourneyDto(
    val id: String,
    val dayNumber: Int,
    val title: String,
    // New field – subtitle, default placeholder for missing JSON
    val subtitle: String = "Placeholder subtitle",
    val category: String,
    val durationMinutes: Int,
    val steps: List<JourneyStepDto>
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
    durationMinutes = durationMinutes,
    steps = steps.map { it.toDomain() }
)

fun JourneyStepDto.toDomain(): JourneyStep = JourneyStep(
    type = type.toDomain(),
    title = title,
    content = content
)

fun StepTypeDto.toDomain(): StepType = StepType.valueOf(name)
