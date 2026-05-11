package com.xapps.question_generator.workflow.pipeline

import com.xapps.question_generator.job.JobProgressReporter
import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class RetryScheduler(
    private val reporter: JobProgressReporter
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun schedule(job: QuestionCreationJob) {
        reporter.requeue(job)

        val delayMillis = computeBackoff(job.attemptCount)

        log.info(
            "Scheduling retry for job ${job.id} in ${delayMillis}ms (attempt ${job.attemptCount})"
        )

        scope.launch {
            delay(delayMillis)

            log.info("Retrying job ${job.id}")
        }
    }

    private fun computeBackoff(attempt: Int): Long {
        val base = 1_000L
        val max = 30_000L
        val exp = (base * (1 shl attempt)).coerceAtMost(max)

        val jitter = (0..500L).random()
        return exp + jitter
    }
}