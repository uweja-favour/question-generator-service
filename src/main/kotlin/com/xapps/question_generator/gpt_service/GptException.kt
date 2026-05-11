package com.xapps.question_generator.gpt_service

sealed class GptException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
    data class NetworkFailure(val attempt: Int, val endpoint: String, val response: String?) :
        GptException("Network failure after $attempt attempts")

    data class NoInternet(val details: String) :
        GptException("No internet access: $details")

    data class Refusal(val reason: String, val payload: Any?) :
        GptException("GPT refused request: $reason")

    data class InvalidContent(val details: String) :
        GptException("Invalid input content: $details")

    data class TokenLimitExceeded(val max: Int, val actual: Int) :
        GptException("Input too large: $actual > $max")

    data class Unexpected(val context: Map<String, Any?>, val raw: String?) :
        GptException("Unexpected GPT error: ${context["errorType"]}")
}