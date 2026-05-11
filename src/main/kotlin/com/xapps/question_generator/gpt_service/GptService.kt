package com.xapps.question_generator.gpt_service

import com.xapps.question_generator.gpt_service.executor.GptExecutor
import kotlinx.serialization.json.JsonObject
import org.springframework.stereotype.Service

@Service
class GptService(
    private val executor: GptExecutor,
    private val modelSelector: GptModelSelector
) : GptClient {

    override suspend fun generateStringResponse(
        prompt: String,
        allowGpt5: Boolean
    ): String {

        val model = modelSelector.selectModel(prompt, allowGpt5)

        val request = GptRequest(
            prompt = prompt,
            model = model,
            responseType = GptResponseType.STRING
        )

        val raw = executor.execute(request)
        return GptResponseParser.extractStringContent(raw)
    }

    override suspend fun generateJsonResponse(
        prompt: String,
        allowGpt5: Boolean
    ): JsonObject {

        val model = modelSelector.selectModel(prompt, allowGpt5)

        val request = GptRequest(
            prompt = prompt,
            model = model,
            responseType = GptResponseType.JSON_OBJECT
        )

        val raw = executor.execute(request)
        return GptResponseParser.extractJsonContent(raw)
    }
}