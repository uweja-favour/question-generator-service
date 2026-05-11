package com.xapps.question_generator.claude_service

enum class ClaudeModel(
    val modelName: String,
    val tier: Tier,
    val contextWindow: Int,
    val maxOutputTokens: Int,
    val supportsTemperature: Boolean = true
) {

    // ===== LATEST PRODUCTION MODELS =====

    CLAUDE_OPUS_4_7(
        "claude-opus-4-7",
        Tier.OPUS,
        contextWindow = 1_000_000,
        maxOutputTokens = 128_000,
        supportsTemperature = false
    ),

    CLAUDE_OPUS_4_6(
        "claude-opus-4-6",
        Tier.OPUS,
        contextWindow = 1_000_000,
        maxOutputTokens = 128_000,
        supportsTemperature = false
    ),

    CLAUDE_SONNET_4_6(
        "claude-sonnet-4-6",
        Tier.SONNET,
        contextWindow = 1_000_000,
        maxOutputTokens = 64_000,
        supportsTemperature = false
    ),

    CLAUDE_OPUS_4_5(
        "claude-opus-4-5",
        Tier.OPUS,
        contextWindow = 1_000_000,
        maxOutputTokens = 128_000
    ),

    CLAUDE_SONNET_4_5(
        "claude-sonnet-4-5",
        Tier.SONNET,
        contextWindow = 1_000_000,
        maxOutputTokens = 64_000
    ),

    CLAUDE_HAIKU_4_5(
        "claude-haiku-4-5",
        Tier.HAIKU,
        contextWindow = 200_000,
        maxOutputTokens = 8_000
    );

    enum class Tier {
        OPUS,
        SONNET,
        HAIKU
    }

    fun maxInputTokens(): Int = contextWindow - maxOutputTokens
}