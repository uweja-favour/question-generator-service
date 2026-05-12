package com.xapps.question_generator.infrastructure.resilience4j

import io.github.resilience4j.retry.RetryRegistry
import io.github.resilience4j.kotlin.retry.executeSuspendFunction
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.checkerframework.checker.units.qual.s
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.seconds

@Component
class RetryExecutor(
    retryRegistry: RetryRegistry
) {

    final val retry = retryRegistry.retry("question-generation")

    final suspend inline fun <T> execute(crossinline block: suspend () -> T): T {
        return retry.executeSuspendFunction {
            block()
        }
    }
}

