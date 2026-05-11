package com.xapps.question_generator.gpt_service

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class OpenAiHttpClient(
    @Value("\${openai.api.key}") private val apiKey: String
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val httpClient: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 300_000 // 5 minutes
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 60_000
            }

            install(Logging) {
                level = LogLevel.INFO
            }

            defaultRequest {
                url("https://api.openai.com/v1/chat/completions")

                header(HttpHeaders.Authorization, "Bearer ${apiKey.trim()}")
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
        }
    }

    suspend fun send(request: GptRequest): String {
        val apiRequest = ChatCompletionRequest(
            model = request.model.modelName,
            messages = listOf(
                Message(role = "user", content = request.prompt)
            ),
            temperature = request.temperature,
            response_format = request.toResponseFormat()
        )

        try {
            val response: HttpResponse = httpClient.post {
                setBody(apiRequest)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                log.error("OpenAI API error: {}", errorBody)
                throw RuntimeException("OpenAI API error: $errorBody")
            }

            return response.bodyAsText()
        } catch (e: Exception) {
            log.error("OpenAI request failed", e)
            throw RuntimeException("OpenAI request failed: ${e.message}", e)
        }
    }

    private fun GptRequest.toResponseFormat(): Map<String, String>? {
        return when (responseType) {
            GptResponseType.JSON_OBJECT -> mapOf("type" to "json_object")
            GptResponseType.STRING -> null
        }
    }

}


@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double,
    val response_format: Map<String, String>? = null
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: Message
)