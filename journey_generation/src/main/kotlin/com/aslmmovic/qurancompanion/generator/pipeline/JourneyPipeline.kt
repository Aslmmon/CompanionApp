package com.aslmmovic.qurancompanion.generator.pipeline

import com.aslmmovic.qurancompanion.generator.ai.GeminiApiClient
import com.aslmmovic.qurancompanion.generator.ai.PromptTemplates
import com.aslmmovic.qurancompanion.generator.config.GeneratorConfig
import com.aslmmovic.qurancompanion.generator.model.GeneratedJourney
import com.aslmmovic.qurancompanion.generator.pdf.PdfTextExtractor
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class JourneyPipeline(
    private val config: GeneratorConfig
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        encodeDefaults = true
    }

    private val geminiClient = GeminiApiClient(
        apiKey = config.apiKey,
        modelName = config.modelName
    )

    // Process a single chunk of pages
    private suspend fun processChunk(
        pdfFile: File,
        language: String,
        startPage: Int,
        endPage: Int,
        startDay: Int
    ): List<GeneratedJourney> {
        val rawText = PdfTextExtractor.extractPageRange(pdfFile, startPage, endPage)

        val rawJson = if (rawText.length > 50) {
            println("🤖 Extracted ${rawText.length} text characters. Sending to AI (${config.modelName})...")
            val prompt = PromptTemplates.buildExtractionPrompt(rawText, language, startDay)
            geminiClient.generateJourneysJson(prompt)
        } else {
            println("🖼️ Scanned image PDF detected. Slicing pages and sending to Gemini Vision AI (${config.modelName})...")
            val pdfBytes = PdfTextExtractor.extractPageRangeToBytes(pdfFile, startPage, endPage)
            val prompt = PromptTemplates.buildExtractionPrompt("Read the attached pages of the Sahaba book.", language, startDay)
            geminiClient.generateJourneysFromPdfBytes(pdfBytes, prompt)
        }

        return json.decodeFromString<List<GeneratedJourney>>(rawJson)
    }

    // Run complete extraction pipeline in batches until target story count is reached
    suspend fun processPdfInBatches(
        pdfFile: File,
        language: String = config.language,
        startDay: Int = config.defaultStartingDayNumber,
        startPage: Int = 5,
        endPage: Int? = null,
        targetCount: Int = 30,
        chunkSize: Int = 15,
        outputFile: File? = null,
        syncToApp: Boolean = false
    ): List<GeneratedJourney> {
        val totalPages = PdfTextExtractor.getPageCount(pdfFile)
        val maxPage = (endPage ?: totalPages).coerceIn(startPage, totalPages)

        val allJourneys = mutableListOf<GeneratedJourney>()
        val seenPersons = mutableSetOf<String>()

        var currentPage = startPage
        println("🚀 Starting batch extraction for target of $targetCount stories (pages $startPage to $maxPage, chunk size: $chunkSize pages)...")

        val targetOutputFile = outputFile ?: resolveDefaultOutputFile(language)
        targetOutputFile.parentFile?.mkdirs()

        while (currentPage <= maxPage && allJourneys.size < targetCount) {
            val chunkEnd = (currentPage + chunkSize - 1).coerceAtMost(maxPage)
            println("\n📖 [Batch ${allJourneys.size + 1}/$targetCount] Processing pages $currentPage to $chunkEnd of $totalPages...")

            try {
                val chunkJourneys = processChunk(
                    pdfFile = pdfFile,
                    language = language,
                    startPage = currentPage,
                    endPage = chunkEnd,
                    startDay = startDay + allJourneys.size
                )

                for (journey in chunkJourneys) {
                    val sanitized = sanitizeJourney(journey, language)
                    val personKey = sanitized.person?.trim() ?: sanitized.title.trim()
                    if (personKey.isNotEmpty() && seenPersons.add(personKey)) {
                        val assignedDay = startDay + allJourneys.size
                        val formattedId = "day_${assignedDay.toString().padStart(3, '0')}"
                        val validated = sanitized.copy(
                            id = formattedId,
                            dayNumber = assignedDay
                        )
                        allJourneys.add(validated)
                        println("  ✨ Added story #${allJourneys.size}: ${validated.person ?: validated.title}")

                        // Progressively save output after each extracted story
                        val formattedJson = json.encodeToString(allJourneys)
                        targetOutputFile.writeText(formattedJson)

                        if (allJourneys.size >= targetCount) break
                    }
                }
            } catch (e: Exception) {
                System.err.println("⚠️ Warning: Batch pages $currentPage-$chunkEnd failed: ${e.message}. Continuing to next batch...")
            }

            currentPage = chunkEnd + 1
            delay(1000)
        }

        println("\n🎉 Batch extraction finished! Total stories extracted: ${allJourneys.size}")
        println("💾 Output saved to: ${targetOutputFile.canonicalPath}")

        if (syncToApp) {
            val candidatePaths = listOf(
                File("shared/src/commonMain/composeResources/files/$language/journeys.json"),
                File("../shared/src/commonMain/composeResources/files/$language/journeys.json")
            )
            val appResourceFile = candidatePaths.firstOrNull { it.parentFile?.exists() == true }
            if (appResourceFile != null) {
                val formattedJson = json.encodeToString(allJourneys)
                appResourceFile.writeText(formattedJson)
                println("🚀 Synced all ${allJourneys.size} stories directly to app resources: ${appResourceFile.canonicalPath}")
            }
        }

        return allJourneys
    }

    // Translate existing Arabic journeys into English in batches and sync to app
    suspend fun translateArabicJourneysToEnglish(
        sourceArabicFile: File,
        outputFile: File? = null,
        syncToApp: Boolean = true,
        batchSize: Int = 3
    ): List<GeneratedJourney> {
        require(sourceArabicFile.exists()) { "Source Arabic journeys file not found: ${sourceArabicFile.absolutePath}" }
        val rawArabicJson = sourceArabicFile.readText()
        val arabicJourneys = json.decodeFromString<List<GeneratedJourney>>(rawArabicJson)

        println("🌐 Starting English translation for ${arabicJourneys.size} Sahaba journeys (batch size: $batchSize)...")
        val allTranslated = mutableListOf<GeneratedJourney>()

        val targetOutputFile = outputFile ?: resolveDefaultOutputFile("en")
        targetOutputFile.parentFile?.mkdirs()

        val chunks = arabicJourneys.chunked(batchSize)
        for ((index, chunk) in chunks.withIndex()) {
            val startDay = chunk.first().dayNumber
            val endDay = chunk.last().dayNumber
            println("\n📖 [Translation Batch ${index + 1}/${chunks.size}] Translating Days $startDay to $endDay...")

            try {
                val chunkJson = json.encodeToString(chunk)
                val prompt = PromptTemplates.buildTranslationPrompt(chunkJson)
                val rawResponse = geminiClient.generateJourneysJson(prompt)
                val translatedChunk = json.decodeFromString<List<GeneratedJourney>>(rawResponse)

                for (journey in translatedChunk) {
                    val sanitized = sanitizeJourney(journey, "en")
                    allTranslated.add(sanitized)
                    println("  ✨ Translated Day ${sanitized.dayNumber}: ${sanitized.person ?: sanitized.title}")
                }

                // Progressively save after each batch
                targetOutputFile.writeText(json.encodeToString(allTranslated))
            } catch (e: Exception) {
                System.err.println("⚠️ Translation batch failed: ${e.message}. Retrying individually...")
                for (singleJourney in chunk) {
                    try {
                        val singleJson = json.encodeToString(listOf(singleJourney))
                        val singlePrompt = PromptTemplates.buildTranslationPrompt(singleJson)
                        val singleRaw = geminiClient.generateJourneysJson(singlePrompt)
                        val singleTranslated = json.decodeFromString<List<GeneratedJourney>>(singleRaw)
                        for (journey in singleTranslated) {
                            val sanitized = sanitizeJourney(journey, "en")
                            allTranslated.add(sanitized)
                            println("  ✨ Translated Day ${sanitized.dayNumber}: ${sanitized.person ?: sanitized.title}")
                        }
                        targetOutputFile.writeText(json.encodeToString(allTranslated))
                    } catch (singleErr: Exception) {
                        System.err.println("❌ Failed to translate single journey Day ${singleJourney.dayNumber}: ${singleErr.message}")
                    }
                }
            }
            delay(1000)
        }

        println("\n🎉 Translation complete! Total translated journeys: ${allTranslated.size}")
        println("💾 Output saved to: ${targetOutputFile.canonicalPath}")

        if (syncToApp) {
            val candidatePaths = listOf(
                File("shared/src/commonMain/composeResources/files/en/journeys.json"),
                File("../shared/src/commonMain/composeResources/files/en/journeys.json")
            )
            val appResourceFile = candidatePaths.firstOrNull { it.parentFile?.exists() == true }
            if (appResourceFile != null) {
                val formattedJson = json.encodeToString(allTranslated)
                appResourceFile.writeText(formattedJson)
                println("🚀 Synced directly to app English resources: ${appResourceFile.canonicalPath}")
            }
        }

        return allTranslated
    }

    // Sanitize bilingual slashes and ensure pure target language strings
    fun sanitizeJourney(journey: GeneratedJourney, language: String): GeneratedJourney {
        val isArabic = language == "ar"

        val sanitizedPerson = if (isArabic) {
            journey.person?.substringBefore(" / ")?.substringBefore(" /")?.trim()
        } else {
            journey.person?.substringAfter(" / ")?.substringAfter("/ ")?.trim()
        } ?: journey.person

        val sanitizedSteps = journey.steps.map { step ->
            val rawTitle = step.title.trim()
            val cleanTitle = if (isArabic) {
                when {
                    rawTitle.contains(" / ") -> rawTitle.substringBefore(" / ").trim()
                    step.type.equals("INTRO", ignoreCase = true) && rawTitle.contains("Introduction", ignoreCase = true) -> "مقدمة"
                    step.type.equals("KEY_LESSONS", ignoreCase = true) && rawTitle.contains("Lessons", ignoreCase = true) -> "الدروس المستفادة"
                    step.type.equals("REFLECTION", ignoreCase = true) && rawTitle.contains("Reflection", ignoreCase = true) -> "تأمل"
                    step.type.equals("ACTION", ignoreCase = true) && rawTitle.contains("Action", ignoreCase = true) -> "عمل اليوم"
                    else -> rawTitle
                }
            } else {
                when {
                    rawTitle.contains(" / ") -> rawTitle.substringAfter(" / ").trim()
                    step.type.equals("INTRO", ignoreCase = true) && rawTitle.contains("مقدمة") -> "Introduction"
                    step.type.equals("KEY_LESSONS", ignoreCase = true) && rawTitle.contains("الدروس") -> "Lessons Learned"
                    step.type.equals("REFLECTION", ignoreCase = true) && rawTitle.contains("تأمل") -> "Reflection"
                    step.type.equals("ACTION", ignoreCase = true) && rawTitle.contains("عمل") -> "Today's Action"
                    else -> rawTitle
                }
            }
            step.copy(title = cleanTitle)
        }

        return journey.copy(
            person = sanitizedPerson,
            steps = sanitizedSteps
        )
    }

    private fun resolveDefaultOutputFile(language: String): File {
        val root = if (File("journey_generation").exists()) File("journey_generation/output_json") else File("output_json")
        return File(root, "journeys_${language}.json")
    }

    fun close() {
        geminiClient.close()
    }
}
