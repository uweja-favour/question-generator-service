package com.xapps.question_generator.claude_service

import com.xapps.question_generator.claude_service.ClaudeHttpClient
import com.xapps.question_generator.gpt_service.AiModelRetryPolicy
import org.springframework.stereotype.Component

@Component
class DefaultClaudeExecutor(
    private val client: ClaudeHttpClient,
    private val retryPolicy: AiModelRetryPolicy
) : ClaudeExecutor {

    override suspend fun execute(request: ClaudeRequest): String {
        return retryPolicy.executeWithRetry(
            action = { attempt ->
                val timeout = computeTimeout(attempt)
                client.send(request)
            }
        )
    }

    private fun computeTimeout(attempt: Int): Long {
        val base = 45_000L
        return (base * (attempt + 1)).coerceAtMost(180_000L)
    }
}

interface ClaudeExecutor {
    suspend fun execute(request: ClaudeRequest): String
}