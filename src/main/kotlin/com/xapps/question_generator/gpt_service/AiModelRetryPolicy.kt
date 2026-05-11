package com.xapps.question_generator.gpt_service

import com.xapps.platform.core.outcome.getOrThrow
import com.xapps.platform.core.outcome.onFailure
import com.xapps.platform.core.outcome.onSuccess
import com.xapps.platform.core.outcome.outcomeOf
import org.springframework.stereotype.Component
import kotlin.math.min
import kotlin.random.Random

/**
 * Retry logic with exponential backoff and jitter.
 */
@Component
data class AiModelRetryPolicy(
    val maxRetries: Int = 3,
    val baseDelayMs: Long = 2000,
    val maxDelayMs: Long = 30_000
) {

    suspend fun <T> executeWithRetry(
        action: suspend (attempt: Int) -> T,
        onFailure: suspend (attempt: Int, Throwable) -> Unit = { _, _ -> }
    ): T {
        repeat(maxRetries - 1) { attempt ->
            val result = outcomeOf { action(attempt + 1) }
            result.onSuccess { return it }
            result.onFailure { error ->
                onFailure(attempt + 1, error.exception)
                kotlinx.coroutines.delay(computeDelay(attempt))
            }
        }
        return outcomeOf { action(maxRetries) }
            .onFailure { error -> onFailure(maxRetries, error.exception) }
            .getOrThrow()
    }

    private fun computeDelay(attempt: Int): Long {
        val backoff = baseDelayMs * (1 shl attempt)
        val jitter = Random.nextLong(0, 1000)
        return min(backoff + jitter, maxDelayMs)
    }
}