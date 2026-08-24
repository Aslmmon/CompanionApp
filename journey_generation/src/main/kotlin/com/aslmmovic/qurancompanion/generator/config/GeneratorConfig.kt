package com.aslmmovic.qurancompanion.generator.config

import java.io.File
import java.util.Properties

data class GeneratorConfig(
    val apiKey: String,
    val modelName: String = "gemini-2.5-flash",
    val language: String = "ar", // "ar" or "en"
    val defaultStartingDayNumber: Int = 1,
    val outputDir: File = File("journey_generation/output_json")
) {
    companion object {
        // Resolve API key from CLI flag, environment variable, or local property files
        fun resolveApiKey(cliApiKey: String? = null): String {
            if (!cliApiKey.isNullOrBlank()) return cliApiKey

            val envKey = System.getenv("GEMINI_API_KEY")
            if (!envKey.isNullOrBlank()) return envKey

            val rootLocalProps = File("local.properties")
            if (rootLocalProps.exists()) {
                val props = Properties().apply { rootLocalProps.inputStream().use { load(it) } }
                val key = props.getProperty("GEMINI_API_KEY") ?: props.getProperty("gemini.api.key")
                if (!key.isNullOrBlank()) return key
            }

            val rootEnv = File(".env")
            if (rootEnv.exists()) {
                rootEnv.readLines().forEach { line ->
                    if (line.startsWith("GEMINI_API_KEY=")) {
                        val key = line.substringAfter("GEMINI_API_KEY=").trim().trim('"', '\'')
                        if (key.isNotBlank()) return key
                    }
                }
            }

            return ""
        }
    }
}
