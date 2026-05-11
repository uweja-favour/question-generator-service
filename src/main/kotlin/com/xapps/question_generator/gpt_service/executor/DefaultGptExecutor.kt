package com.xapps.question_generator.gpt_service.executor

import com.xapps.question_generator.gpt_service.GptRequest
import com.xapps.question_generator.gpt_service.AiModelRetryPolicy
import com.xapps.question_generator.gpt_service.OpenAiHttpClient
import org.springframework.stereotype.Component

@Component
class DefaultGptExecutor(
    private val client: OpenAiHttpClient,
    private val retryPolicy: AiModelRetryPolicy
) : GptExecutor {

    override suspend fun execute(request: GptRequest): String {
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