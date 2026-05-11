package com.xapps.question_generator.gpt_service

/**
 * GPT model definitions with conservative token limits.
 */
enum class GptModel(
    val modelName: String,
    val maxInputTokens: Int,
    val maxOutputTokens: Int
) {
    GPT3_4K("gpt-3.5-turbo", 4096, 4096),
    GPT3_16K("gpt-3.5-turbo-16k", 16384, 4096),
    GPT4_8K("gpt-4", 8192, 4096),
    GPT4_32K("gpt-4-32k", 32768, 4096),
    GPT4_TURBO("gpt-4-turbo", 128000, 4096),
    GPT4O("gpt-4o", 128000, 4096),
    GPT5("gpt-5", 272000, 128000);

    val totalContextTokens: Int
        get() = maxInputTokens + maxOutputTokens
}