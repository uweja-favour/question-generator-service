package com.xapps.question_generator.claude_service

data class ClaudeRequest(
    val prompt: String,
    val model: ClaudeModel,
    val temperature: Double = 0.1,
    val responseType: ClaudeResponseType = ClaudeResponseType.STRING
)

enum class ClaudeResponseType {
    STRING,
    JSON_OBJECT
}