package com.aslmmovic.qurancompanion.generator

import com.aslmmovic.qurancompanion.generator.config.GeneratorConfig
import com.aslmmovic.qurancompanion.generator.pipeline.JourneyPipeline
import kotlinx.coroutines.runBlocking
import java.io.File

fun main(args: Array<String>) = runBlocking {
    println("==========================================================")
    println("  Sahaba Companions - Sahaba Journey AI Generation Tool  ")
    println("==========================================================")

    var pdfPath: String? = null
    var language = "ar"
    var startDay = 1
    var startPage = 5
    var endPage: Int? = null
    var targetCount = 30
    var chunkSize = 15
    var outputPath: String? = null
    var modelName = "gemini-2.5-flash"
    var apiKeyArg: String? = null
    var syncToApp = false
    var translateMode = false
    var sourceArabicPath: String? = null

    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "--translate" -> translateMode = true
            "--source" -> if (i + 1 < args.size) sourceArabicPath = args[++i]
            "--pdf" -> if (i + 1 < args.size) pdfPath = args[++i]
            "--lang" -> if (i + 1 < args.size) language = args[++i]
            "--start-day" -> if (i + 1 < args.size) startDay = args[++i].toIntOrNull() ?: 1
            "--start-page" -> if (i + 1 < args.size) startPage = args[++i].toIntOrNull() ?: 5
            "--end-page" -> if (i + 1 < args.size) endPage = args[++i].toIntOrNull()
            "--target-count", "--count" -> if (i + 1 < args.size) targetCount = args[++i].toIntOrNull() ?: 30
            "--chunk-size" -> if (i + 1 < args.size) chunkSize = args[++i].toIntOrNull() ?: 15
            "--output" -> if (i + 1 < args.size) outputPath = args[++i]
            "--model" -> if (i + 1 < args.size) modelName = args[++i]
            "--api-key" -> if (i + 1 < args.size) apiKeyArg = args[++i]
            "--sync-app" -> syncToApp = true
        }
        i++
    }

    val resolvedApiKey = GeneratorConfig.resolveApiKey(apiKeyArg)
    if (resolvedApiKey.isBlank()) {
        System.err.println("❌ ERROR: No Gemini API key provided.")
        System.err.println("Please provide your API key via:")
        System.err.println("  1. Environment variable: export GEMINI_API_KEY=\"your_key\"")
        System.err.println("  2. In local.properties: GEMINI_API_KEY=your_key")
        System.err.println("  3. Via CLI argument: --api-key your_key")
        return@runBlocking
    }

    val config = GeneratorConfig(
        apiKey = resolvedApiKey,
        modelName = modelName,
        language = language,
        defaultStartingDayNumber = startDay
    )

    val pipeline = JourneyPipeline(config)
    try {
        if (translateMode) {
            val sourceFile = if (sourceArabicPath != null) {
                resolveFile(sourceArabicPath)
            } else {
                listOf(
                    File("shared/src/commonMain/composeResources/files/ar/journeys.json"),
                    File("../shared/src/commonMain/composeResources/files/ar/journeys.json"),
                    File("journey_generation/output_json/journeys_ar.json"),
                    File("output_json/journeys_ar.json")
                ).firstOrNull { it.exists() } ?: File("journey_generation/output_json/journeys_ar.json")
            }

            pipeline.translateArabicJourneysToEnglish(
                sourceArabicFile = sourceFile,
                outputFile = outputPath?.let { resolveOutputFile(it) },
                syncToApp = syncToApp
            )
        } else {
            // Resolve PDF file to process robustly regardless of current execution directory
            val targetPdfFile = if (pdfPath != null) {
                resolveFile(pdfPath)
            } else {
                val possibleDirs = listOf(
                    File("journey_generation/input_pdfs"),
                    File("input_pdfs"),
                    File("../journey_generation/input_pdfs")
                )
                val inputDir = possibleDirs.firstOrNull { it.exists() && it.isDirectory } ?: File("journey_generation/input_pdfs")
                val pdfs = inputDir.listFiles { _, name -> name.endsWith(".pdf", ignoreCase = true) }
                if (pdfs.isNullOrEmpty()) {
                    println("ℹ️ No PDF path specified and no PDFs found in '${inputDir.path}'.")
                    println("Usage: ./journey_generation/generate_journeys.sh --pdf path/to/book.pdf --lang ar")
                    return@runBlocking
                }
                pdfs.first()
            }

            if (!targetPdfFile.exists()) {
                System.err.println("❌ ERROR: File not found: ${targetPdfFile.absolutePath}")
                return@runBlocking
            }

            pipeline.processPdfInBatches(
                pdfFile = targetPdfFile,
                language = language,
                startDay = startDay,
                startPage = startPage,
                endPage = endPage,
                targetCount = targetCount,
                chunkSize = chunkSize,
                outputFile = outputPath?.let { resolveOutputFile(it) },
                syncToApp = syncToApp
            )
        }
        println("✨ Operation complete successfully!")
    } catch (e: Exception) {
        System.err.println("❌ Operation failed: ${e.message}")
        e.printStackTrace()
    } finally {
        pipeline.close()
    }
}

// Resilient file path resolution across execution directory contexts
private fun resolveFile(path: String): File {
    val direct = File(path)
    if (direct.exists()) return direct

    val stripped = File(path.removePrefix("journey_generation/").removePrefix("journey_generation\\"))
    if (stripped.exists()) return stripped

    val prefixed = File("journey_generation", path)
    if (prefixed.exists()) return prefixed

    val parentPrefixed = File("..", path)
    if (parentPrefixed.exists()) return parentPrefixed

    return direct
}

// Resilient output file path resolution
private fun resolveOutputFile(path: String): File {
    val direct = File(path)
    if (direct.isAbsolute) return direct

    val root = if (File("journey_generation").exists()) File(".") else File("..")
    return File(root, path)
}
