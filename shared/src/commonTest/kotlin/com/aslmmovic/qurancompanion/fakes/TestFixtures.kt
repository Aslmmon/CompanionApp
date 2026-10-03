package com.aslmmovic.qurancompanion.fakes

import com.aslmmovic.qurancompanion.domain.model.Cover
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.model.JourneyStep
import com.aslmmovic.qurancompanion.domain.model.StepType
import com.aslmmovic.qurancompanion.domain.model.UserPreferences

/**
 * Convenience factory for creating test [Journey] instances.
 */
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
    cover: Cover = Cover("illustration", "asset"),
    steps: List<JourneyStep> = listOf(
        JourneyStep(StepType.INTRO, "Intro Title", "Intro content"),
        JourneyStep(StepType.ACTION, "Action Title", "Action content")
    ),
    references: List<String> = emptyList(),
    tags: List<String> = emptyList()
) = Journey(
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

/**
 * Convenience factory for creating test [UserPreferences] instances.
 */
fun testUserPreferences(
    reminderHour: Int = 8,
    reminderMinute: Int = 0,
    isReminderEnabled: Boolean = true,
    preferredLanguage: String? = null,
    isDarkMode: Boolean? = null
) = UserPreferences(
    reminderHour = reminderHour,
    reminderMinute = reminderMinute,
    isReminderEnabled = isReminderEnabled,
    preferredLanguage = preferredLanguage,
    isDarkMode = isDarkMode
)

/**
 * Convenience factory for creating test [Sahaba] instances.
 */
fun testSahaba(
    id: String = "abu_bakr",
    name: String = "Abu Bakr as-Siddiq",
    arabicName: String = "أبو بكر الصديق",
    initialLetterArabic: String = "أ",
    epithet: String = "As-Siddiq (The Truthful)",
    category: com.aslmmovic.qurancompanion.domain.model.SahabaCategory = com.aslmmovic.qurancompanion.domain.model.SahabaCategory.TEN_PROMISED,
    journeyCount: Int = 14,
    bio: String = "First caliph of Islam"
) = com.aslmmovic.qurancompanion.domain.model.Sahaba(
    id = id,
    name = name,
    arabicName = arabicName,
    initialLetterArabic = initialLetterArabic,
    epithet = epithet,
    category = category,
    journeyCount = journeyCount,
    bio = bio
)
