package com.xapps.question_generator.job

import com.xapps.platform.core.outcome.onFailure
import com.xapps.platform.core.outcome.outcomeOf
import com.xapps.question_generator.job.domain.model.CreationJob
import com.xapps.question_generator.job.service.CreationJobService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

interface ProgressReporter {
    suspend fun update(job: CreationJob, progress: Int)
    suspend fun fail(job: CreationJob, exception: Throwable, canRetry: Boolean)
    suspend fun complete(job: CreationJob)
    suspend fun requeue(job: CreationJob)
}

@Component
class JobProgressReporter(
    private val jobService: CreationJobService,
    private val jobUpdatePublisher: JobUpdatePublisher
) : ProgressReporter {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun update(job: CreationJob, progress: Int) =
        report(job) { markRunning(progress) }

    override suspend fun fail(
        job: CreationJob,
        exception: Throwable,
        canRetry: Boolean
    ) = report(job) {
        markFailed(exception.message.toString(), canRetry)
    }

    override suspend fun complete(job: CreationJob) =
        report(job) { markCompleted() }

    override suspend fun requeue(job: CreationJob) =
        report(job) { markQueued() }

    private suspend fun report(
        job: CreationJob,
        transition: CreationJob.() -> CreationJob
    ) {
        outcomeOf {
            val updatedJob = job.transition()

            jobService.save(updatedJob)
//            jobUpdatePublisher.publish(updatedJob.id)
        }.onFailure { error ->
            log.error(
                "Failed to report job state change for job ID: ${job.id}: ${error.message}",
                error.exception
            )
        }
    }
}
