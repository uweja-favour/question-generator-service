package com.xapps.question_generator.gpt_service

data class GptRequest(
    val prompt: String,
    val model: GptModel,
    val temperature: Double = 0.1,
    val responseType: GptResponseType = GptResponseType.STRING
)

enum class GptResponseType {
    STRING,
    JSON_OBJECT
}