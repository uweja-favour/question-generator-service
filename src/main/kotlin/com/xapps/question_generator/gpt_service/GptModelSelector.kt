package com.xapps.question_generator.gpt_service

import org.springframework.stereotype.Component

@Component
class GptModelSelector(
    private val tokenCounter: LocalTokenCounter
) {

    fun selectModel(
        prompt: String,
        allowGpt5: Boolean
    ): GptModel {
        val eligibleModels = eligibleModels(allowGpt5)
        val estimatedInputTokens = estimateInputTokens(prompt)

        return eligibleModels
            .firstOrNull { it.canHandlePrompt(prompt, tokenCounter) }
            ?: throw tokenLimitExceeded(eligibleModels, estimatedInputTokens)
    }

    private fun eligibleModels(allowGpt5: Boolean): List<GptModel> =
        GptModel.entries
            .let { if (allowGpt5) it else it - GptModel.GPT5 }
            .sortedBy(GptModel::totalContextTokens)

    private fun GptModel.canHandlePrompt(
        prompt: String,
        tokenCounter: LocalTokenCounter
    ): Boolean {
        val inputTokens = tokenCounter.countTokens(prompt, modelName)
        val totalTokens = inputTokens + SYSTEM_OVERHEAD_TOKENS
        return totalTokens < totalContextTokens
    }

    private fun estimateInputTokens(prompt: String): Int =
        tokenCounter.countTokensWithUniversalEncoding(prompt)

    private fun tokenLimitExceeded(
        models: List<GptModel>,
        actualTokens: Int
    ): GptException.TokenLimitExceeded =
        GptException.TokenLimitExceeded(
            max = models.maxOf(GptModel::maxInputTokens),
            actual = actualTokens
        )

    private companion object {
        private const val SYSTEM_OVERHEAD_TOKENS = 16
    }
}
