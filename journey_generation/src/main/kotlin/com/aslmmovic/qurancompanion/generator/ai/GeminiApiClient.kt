package com.aslmmovic.qurancompanion.generator.ai

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Base64

class GeminiApiClient(
    private val apiKey: String,
    private val modelName: String = "gemini-flash-latest"
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 300_000 // 5 minutes for multimodal page processing
            socketTimeoutMillis = 300_000
            connectTimeoutMillis = 60_000
        }
    }

    // Call Gemini generateContent API with text prompt
    suspend fun generateJourneysJson(prompt: String): String {
        return executeGeminiRequest(listOf(GeminiPart(text = prompt)))
    }

    // Call Gemini generateContent API with multimodal PDF attachment for scanned documents
    suspend fun generateJourneysFromPdfBytes(pdfBytes: ByteArray, prompt: String): String {
        val base64Data = Base64.getEncoder().encodeToString(pdfBytes)
        val parts = listOf(
            GeminiPart(text = prompt),
            GeminiPart(inline_data = GeminiInlineData(mime_type = "application/pdf", data = base64Data))
        )
        return executeGeminiRequest(parts)
    }

    private suspend fun executeGeminiRequest(parts: List<GeminiPart>): String {
        require(apiKey.isNotBlank()) {
            "Gemini API key is required. Provide it via GEMINI_API_KEY environment variable or local.properties."
        }

        val candidateModels = listOf(
            modelName,
            "gemini-flash-latest",
            "gemini-flash-lite-latest",
            "gemini-2.5-flash-lite",
            "gemini-3.1-flash-lite",
            "gemini-2.5-flash",
            "gemini-2.5-pro",
            "gemini-pro-latest"
        ).distinct()

        var lastError = ""

        for (candidate in candidateModels) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$candidate:generateContent?key=$apiKey"

            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(parts = parts)),
                generationConfig = GeminiGenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.3
                )
            )

            for (attempt in 1..2) {
                try {
                    val response = httpClient.post(url) {
                        contentType(ContentType.Application.Json)
                        setBody(requestBody)
                    }

                    val rawResponseText = response.bodyAsText()
                    if (response.status.value in 200..299) {
                        return extractTextFromGeminiResponse(rawResponseText)
                    } else if (response.status.value == 429) {
                        lastError = "Rate limit [HTTP 429] on model $candidate"
                        println("⚠️ Rate limit on $candidate. Pausing 10s before retry...")
                        delay(10000)
                    } else {
                        lastError = "Gemini API error [HTTP ${response.status.value}] with model $candidate: $rawResponseText"
                        break
                    }
                } catch (e: Exception) {
                    lastError = "Request failed for model $candidate: ${e.message}"
                    break
                }
            }
        }

        throw IllegalStateException("All candidate Gemini models failed. Last error: $lastError")
    }

    // Parse candidate text from Gemini response JSON
    private fun extractTextFromGeminiResponse(rawResponse: String): String {
        val root = json.parseToJsonElement(rawResponse).jsonObject
        val candidates = root["candidates"]?.jsonArray
            ?: throw IllegalStateException("No candidates found in response: $rawResponse")

        if (candidates.isEmpty()) throw IllegalStateException("Candidates list is empty in response.")

        val firstCandidate = candidates[0].jsonObject
        val content = firstCandidate["content"]?.jsonObject
            ?: throw IllegalStateException("No content object found in candidate.")

        val parts = content["parts"]?.jsonArray
            ?: throw IllegalStateException("No parts found in candidate content.")

        val firstPart = parts[0].jsonObject
        return firstPart["text"]?.jsonPrimitive?.content
            ?: throw IllegalStateException("No text found in candidate part.")
    }

    fun close() {
        httpClient.close()
    }
}

@Serializable
private data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
private data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
private data class GeminiPart(
    val text: String? = null,
    val inline_data: GeminiInlineData? = null
)

@Serializable
private data class GeminiInlineData(
    val mime_type: String,
    val data: String
)

@Serializable
private data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Double? = null
)
