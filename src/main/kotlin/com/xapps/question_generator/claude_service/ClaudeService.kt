package com.xapps.question_generator.claude_service

import kotlinx.serialization.json.JsonObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ClaudeService(
    private val executor: ClaudeExecutor,
    private val modelSelector: ClaudeModelSelector
) : ClaudeClient {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun generateStringResponse(prompt: String): String {
        val model = modelSelector.selectModel(prompt)
        log.info("Selected model: $model")

        val request = ClaudeRequest(
            prompt = prompt,
            model = model,
            responseType = ClaudeResponseType.STRING
        )

        val raw = executor.execute(request)
        return ClaudeResponseParser.extractStringContent(raw)
    }

    override suspend fun generateJsonResponse(prompt: String): JsonObject {
        val model = modelSelector.selectModel(prompt)
        log.info("Selected model: $model")

        val request = ClaudeRequest(
            prompt = prompt,
            model = model,
            responseType = ClaudeResponseType.JSON_OBJECT
        )

        val raw = executor.execute(request)
        return ClaudeResponseParser.extractJsonContent(raw)
    }
}