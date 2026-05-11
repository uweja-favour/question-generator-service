package com.xapps.question_generator.gpt_service

import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory

/**
 * Responsible for extracting and cleaning GPT content from raw responses.
 */
object GptResponseParser {

    private val log = LoggerFactory.getLogger(GptResponseParser.javaClass)

    /**
     * Extracts raw string content from GPT response.
     */
    fun extractStringContent(responseText: String): String {
        val root = runCatching {
            Json.parseToJsonElement(responseText).jsonObject
        }.getOrElse {
            throw RuntimeException("Invalid OpenAI response JSON envelope: ${it.message}. The invalid response: $responseText", it)
        }

        val error = root["error"]
        if (error != null) {
            val message = error.jsonObject["message"]?.jsonPrimitive?.content
                ?: "Unknown OpenAI API error"
            throw RuntimeException("OpenAI API Error: $message")
        }

        val choice = root["choices"]?.jsonArray?.firstOrNull()?.jsonObject
            ?: throw RuntimeException("OpenAI response missing choices array")

        val finishReason = choice["finish_reason"]?.jsonPrimitive?.content

        if (finishReason == "length") {
            log.error("GPT response was truncated (finish_reason=length). Refusing to parse.")
            throw RuntimeException("GPT response truncated — cannot safely parse output")
        }

        val content = choice["message"]?.jsonObject
            ?.get("content")
            ?.jsonPrimitive
            ?.content
            ?: throw RuntimeException("OpenAI response missing message content")

        return normalizeContent(content)
    }

    /**
     * Extracts content and parses it as JSON.
     */
    fun extractJsonContent(responseText: String): JsonObject {
        val content = extractStringContent(responseText)
        return runCatching {
            Json.parseToJsonElement(content).jsonObject
        }.getOrElse {
            throw RuntimeException(
                "Failed to parse GPT content as JSON. Content was not valid JSON: ${it.message}\nRaw content:\n$content",
                it
            )
        }
    }

    private fun normalizeContent(content: String): String {
        return content
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }
}
