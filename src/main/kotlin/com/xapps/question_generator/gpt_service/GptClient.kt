package com.xapps.question_generator.gpt_service

import kotlinx.serialization.json.JsonObject

interface GptClient {

    /**
     * Sends a prompt to GPT and expects a JSON response.
     * Throws if GPT returns invalid JSON.
     */
    suspend fun generateJsonResponse(prompt: String, allowGpt5: Boolean = false): JsonObject

    /**
     * Sends a prompt to GPT and expects a plain string response.
     */
    suspend fun generateStringResponse(prompt: String, allowGpt5: Boolean = false): String
}