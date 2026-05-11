package com.xapps.question_generator.claude_service

import kotlinx.serialization.*

@Serializable
data class ClaudeApiRequest(
    val model: String,
    val max_tokens: Int,
    val temperature: Double?,
    val messages: List<ClaudeMessage>
)

@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String
)

@Serializable
data class ClaudeApiResponse(
    val content: List<ClaudeContentBlock>,
    val stop_reason: String? = null
)

@Serializable
data class ClaudeContentBlock(
    val type: String,
    val text: String? = null
)