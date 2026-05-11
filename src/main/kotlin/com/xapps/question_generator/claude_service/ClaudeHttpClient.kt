package com.xapps.question_generator.claude_service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ClaudeHttpClient(
    @Value("\${anthropic.api.key}") private val apiKey: String
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @OptIn(ExperimentalSerializationApi::class)
    private val httpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true

                    encodeDefaults = false
                    explicitNulls = false
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 300_000
                connectTimeoutMillis = 50_000
                socketTimeoutMillis = 300_000
            }

            install(Logging) {
                level = LogLevel.INFO
            }

            defaultRequest {
                url("https://api.anthropic.com/v1/messages")

                header("x-api-key", apiKey.trim())
                header("anthropic-version", "2023-06-01")

                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
        }
    }

    suspend fun send(request: ClaudeRequest): String {
        val apiRequest = ClaudeApiRequest(
            model = request.model.modelName,
            max_tokens = request.model.maxOutputTokens,
            messages = listOf(
                ClaudeMessage(
                    role = "user",
                    content = buildPrompt(request)
                )
            ),
            temperature = if (request.model.supportsTemperature)
                request.temperature
            else
                null
        )

        try {
            val response: HttpResponse = httpClient.post {
                setBody(apiRequest)
            }

            val body = response.bodyAsText()

            if (!response.status.isSuccess()) {
                log.error("Claude API error: {}", body)
                throw RuntimeException("Claude API error: $body")
            }

            return body
        } catch (e: Exception) {
            log.error("Claude request failed", e)
            throw RuntimeException("Claude request failed: ${e.message}", e)
        }
    }

    /**
     * Enforces JSON output deterministically.
     */
    private fun buildPrompt(request: ClaudeRequest): String {
        return when (request.responseType) {
            ClaudeResponseType.JSON_OBJECT -> """
                You MUST return a valid JSON object only.
                Do NOT include explanations, markdown, or extra text.
                
                ${request.prompt}
            """.trimIndent()

            ClaudeResponseType.STRING -> request.prompt
        }
    }
}