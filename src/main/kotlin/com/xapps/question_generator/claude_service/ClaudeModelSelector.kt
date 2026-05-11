package com.xapps.question_generator.claude_service

import com.xapps.question_generator.gpt_service.LocalTokenCounter
import org.springframework.stereotype.Component

@Component
class ClaudeModelSelector(
    private val tokenCounter: LocalTokenCounter
) {

//    fun selectModel(prompt: String): ClaudeModel {
//        val estimatedTokens = tokenCounter.countTokensWithUniversalEncoding(prompt)
//
//        return ClaudeModel.entries
//            .sortedBy { it.contextWindow }
//            .firstOrNull { model ->
//                estimatedTokens < model.maxInputTokens()
//            }
//            ?: throw RuntimeException(
//                "Claude token limit exceeded. Input tokens=$estimatedTokens, max supported=${ClaudeModel.entries.maxOf { it.contextWindow }}"
//            )
//    }

    fun selectModel(prompt: String): ClaudeModel {
        return ClaudeModel.CLAUDE_OPUS_4_7
    }
}