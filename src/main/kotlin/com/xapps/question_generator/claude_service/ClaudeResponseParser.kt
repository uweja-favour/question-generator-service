package com.xapps.question_generator.claude_service

import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory

object ClaudeResponseParser {

    private val log = LoggerFactory.getLogger(javaClass)

    fun extractStringContent(responseText: String): String {
        val root = parseRoot(responseText)

        val contentArray = root["content"]?.jsonArray
            ?: throw RuntimeException("Claude response missing content array")

        val text = contentArray
            .mapNotNull { it.jsonObject["text"]?.jsonPrimitive?.content }
            .joinToString("\n")
            .trim()

        if (text.isBlank()) {
            throw RuntimeException("Claude returned empty content")
        }

        validateStopReason(root)

        return normalize(text)
    }

    fun extractJsonContent(responseText: String): JsonObject {
        val content = extractStringContent(responseText)

        return runCatching {
            Json.parseToJsonElement(content).jsonObject
        }.getOrElse {
            log.error("Invalid JSON from Claude: {}", content)
            throw RuntimeException(
                "Claude returned invalid JSON: ${it.message}\n$content",
                it
            )
        }
    }

    private fun parseRoot(responseText: String): JsonObject {
        return runCatching {
            Json.parseToJsonElement(responseText).jsonObject
        }.getOrElse {
            throw RuntimeException("Invalid Claude response envelope: ${it.message}", it)
        }
    }

    private fun validateStopReason(root: JsonObject) {
        val stopReason = root["stop_reason"]?.jsonPrimitive?.content

        if (stopReason == "max_tokens") {
            log.error("Claude response truncated")
            throw RuntimeException("Claude response truncated — unsafe to parse")
        }
    }

    private fun normalize(content: String): String {
        return content
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }
}