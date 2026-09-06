package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.fakes.FakeJourneyRepository as ActualFakeJourneyRepository
import com.aslmmovic.qurancompanion.fakes.testJourney as actualTestJourney

typealias FakeJourneyRepository = ActualFakeJourneyRepository

fun testJourney(
    id: String = "test-journey-1",
    dayNumber: Int = 1,
    title: String = "Test Journey",
    subtitle: String = "Placeholder subtitle",
    category: String = "Test",
    person: String? = null,
    emotion: String = "Peace",
    theme: String = "Night",
    heroQuote: String = "Quote",
    intention: String = "Intention",
    durationMinutes: Int = 5,
    difficulty: String = "Easy",
    estimatedReadingMinutes: Int = 4,
    cover: com.aslmmovic.qurancompanion.domain.model.Cover = com.aslmmovic.qurancompanion.domain.model.Cover("illustration", "asset"),
    steps: List<com.aslmmovic.qurancompanion.domain.model.JourneyStep> = listOf(
        com.aslmmovic.qurancompanion.domain.model.JourneyStep(com.aslmmovic.qurancompanion.domain.model.StepType.INTRO, "Intro Title", "Intro content"),
        com.aslmmovic.qurancompanion.domain.model.JourneyStep(com.aslmmovic.qurancompanion.domain.model.StepType.ACTION, "Action Title", "Action content")
    ),
    references: List<String> = emptyList(),
    tags: List<String> = emptyList()
) = actualTestJourney(
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
    cover = cover,
    steps = steps,
    references = references,
    tags = tags
)
