package com.aslmmovic.qurancompanion.generator

import com.aslmmovic.qurancompanion.generator.ai.PromptTemplates
import com.aslmmovic.qurancompanion.generator.config.GeneratorConfig
import com.aslmmovic.qurancompanion.generator.model.GeneratedCover
import com.aslmmovic.qurancompanion.generator.model.GeneratedJourney
import com.aslmmovic.qurancompanion.generator.model.GeneratedStep
import com.aslmmovic.qurancompanion.generator.pipeline.JourneyPipeline
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JourneySerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        encodeDefaults = true
    }

    @Test
    fun `GeneratedJourney serialization and deserialization matches schema`() {
        val sampleJourney = GeneratedJourney(
            id = "day_001",
            dayNumber = 1,
            title = "He Chose the Hereafter Over This World",
            subtitle = "One scene changed the life of a young man from Quraysh forever.",
            category = "The Companions",
            person = "Saeed ibn Amir al-Jumahi",
            emotion = "Sacrifice",
            theme = "Sunrise",
            heroQuote = "«Saeed ibn Amir was a man who chose the Hereafter over this world.»",
            intention = "Read today's journey and reflect on sincerity.",
            durationMinutes = 10,
            difficulty = "Easy",
            estimatedReadingMinutes = 8,
            cover = GeneratedCover(type = "illustration", asset = "saeed_ibn_amir"),
            steps = listOf(
                GeneratedStep(type = "INTRO", title = "Introduction", content = "Intro content"),
                GeneratedStep(type = "STORY", title = "A Scene That Changed a Life", content = "Story content"),
                GeneratedStep(type = "KEY_LESSONS", title = "Lessons Learned", content = "1. Lesson one"),
                GeneratedStep(type = "REFLECTION", title = "Reflection", content = "Reflective question"),
                GeneratedStep(type = "ACTION", title = "Today's Action", content = "Action deed")
            ),
            references = listOf("Al-Isabah", "History of Islam"),
            tags = listOf("Saeed ibn Amir", "Sacrifice")
        )

        val jsonString = json.encodeToString(listOf(sampleJourney))
        val decoded = json.decodeFromString<List<GeneratedJourney>>(jsonString)

        assertEquals(1, decoded.size)
        val first = decoded.first()
        assertEquals("day_001", first.id)
        assertEquals(1, first.dayNumber)
        assertEquals("Saeed ibn Amir al-Jumahi", first.person)
        assertEquals(5, first.steps.size)
        assertEquals("INTRO", first.steps[0].type)
        assertEquals("STORY", first.steps[1].type)
        assertEquals("KEY_LESSONS", first.steps[2].type)
        assertEquals("REFLECTION", first.steps[3].type)
        assertEquals("ACTION", first.steps[4].type)
    }

    @Test
    fun `PromptTemplates builds appropriate prompt for Arabic and English`() {
        val arabicPrompt = PromptTemplates.buildExtractionPrompt("نص السيرة", "ar", 5)
        assertTrue(arabicPrompt.contains("فصحى بليغة"))
        assertTrue(arabicPrompt.contains("الصحابة"))
        assertTrue(arabicPrompt.contains("\"dayNumber\": 5"))

        val englishPrompt = PromptTemplates.buildExtractionPrompt("Biography text", "en", 1)
        assertTrue(englishPrompt.contains("The Companions"))
        assertTrue(englishPrompt.contains("\"dayNumber\": 1"))
    }

    @Test
    fun `sanitizeJourney strips bilingual slashes and cleans Arabic titles`() {
        val pipeline = JourneyPipeline(GeneratorConfig(apiKey = "dummy"))
        val uncleaned = GeneratedJourney(
            id = "day_001",
            dayNumber = 1,
            title = "سعيد بن عامر",
            subtitle = "وصف",
            category = "الصحابة",
            person = "سعيد بن عامر الجمحي / Saeed ibn Amir",
            emotion = "الزهد",
            theme = "Golden",
            heroQuote = "اقتباس",
            intention = "نية",
            cover = GeneratedCover(asset = "saeed_ibn_amir"),
            steps = listOf(
                GeneratedStep(type = "INTRO", title = "مقدمة / Introduction", content = "محتوى"),
                GeneratedStep(type = "KEY_LESSONS", title = "الدروس المستفادة / Lessons Learned", content = "محتوى"),
                GeneratedStep(type = "REFLECTION", title = "تأمل / Reflection", content = "محتوى"),
                GeneratedStep(type = "ACTION", title = "عمل اليوم / Today's Action", content = "محتوى")
            )
        )

        val cleaned = pipeline.sanitizeJourney(uncleaned, "ar")
        assertEquals("سعيد بن عامر الجمحي", cleaned.person)
        assertEquals("مقدمة", cleaned.steps[0].title)
        assertEquals("الدروس المستفادة", cleaned.steps[1].title)
        assertEquals("تأمل", cleaned.steps[2].title)
        assertEquals("عمل اليوم", cleaned.steps[3].title)
    }

    @Test
    fun `sanitize and clean existing journeys_ar json files`() {
        val pipeline = JourneyPipeline(GeneratorConfig(apiKey = "dummy"))
        val candidatePaths = listOf(
            File("journey_generation/output_json/journeys_ar.json"),
            File("output_json/journeys_ar.json"),
            File("../journey_generation/output_json/journeys_ar.json"),
            File("shared/src/commonMain/composeResources/files/ar/journeys.json"),
            File("../shared/src/commonMain/composeResources/files/ar/journeys.json")
        )

        for (file in candidatePaths) {
            if (file.exists()) {
                val raw = file.readText()
                val list = json.decodeFromString<List<GeneratedJourney>>(raw)
                val sanitized = list.map { pipeline.sanitizeJourney(it, "ar") }
                file.writeText(json.encodeToString(sanitized))
            }
        }
    }
}
