package com.xapps.question_generator.infrastructure.resilience4j

import com.xapps.question_generator.workflow.pipeline.TransientFailureClassifier
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class RetryConfiguration(
    private val transientClassifier: TransientFailureClassifier
) {

    @Bean
    fun retryRegistry(): RetryRegistry {
//        val config = RetryConfig.custom<Any>()
//            .maxAttempts(3)
//            .waitDuration(Duration.ofSeconds(1))
//            .intervalFunction { attempt ->
//                // exponential backoff: 1s, 2s, 4s...
//                (1000L * (1 shl (attempt - 1))).coerceAtMost(30_000L)
//            }
//            .retryOnException { ex ->
//                transientClassifier.isTransient(ex)
//            }
//            .failAfterMaxAttempts(true)
//            .build()


        val config = RetryConfig.custom<Any>()
            .maxAttempts(3)
            .retryOnException { ex ->
                transientClassifier.isTransient(ex)
            }
            .intervalFunction { attempt ->
                val base = 1_000L
                val max = 30_000L

                val exponential = base * (1 shl (attempt - 1))
                exponential.coerceAtMost(max)
            }
            .build()


        return RetryRegistry.of(config)
    }
}