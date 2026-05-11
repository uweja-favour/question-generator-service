package com.xapps.question_generator.claude_service

import kotlinx.serialization.json.JsonObject

interface ClaudeClient {

    suspend fun generateStringResponse(prompt: String): String

    suspend fun generateJsonResponse(prompt: String): JsonObject
}