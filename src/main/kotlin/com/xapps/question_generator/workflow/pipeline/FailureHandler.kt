package com.xapps.question_generator.workflow.pipeline

import com.xapps.question_generator.job.JobProgressReporter
import com.xapps.question_generator.job.service.QuestionCreationJobService
import com.xapps.question_generator.workflow.JobProcessor
import com.xapps.questions.contracts.question_generation.JobId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FailureHandler(
    private val jobService: QuestionCreationJobService,
    private val reporter: JobProgressReporter,
    private val retryScheduler: RetryScheduler,
    private val transientClassifier: TransientFailureClassifier,
    private val retryPolicy: RetryPolicy
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun handle(jobId: JobId, ex: Throwable) {
        val job = jobService.findById(jobId) ?: run {
            log.info("${javaClass.simpleName} - failed to find job (JobID=$jobId)")
            return
        }

        if (job.attemptCount < retryPolicy.maxAttempts && transientClassifier.isTransient(ex)) {
            retryScheduler.schedule(job)
        } else
            reporter.fail(job, ex, canRetry = false)

    }
}
