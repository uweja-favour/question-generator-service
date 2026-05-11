package com.xapps.question_generator.job

import com.xapps.platform.core.outcome.onFailure
import com.xapps.platform.core.outcome.outcomeOf
import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import com.xapps.question_generator.job.service.QuestionCreationJobService
import com.xapps.model.QuizId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

interface ProgressReporter {
    suspend fun update(job: QuestionCreationJob, progress: Int)
    suspend fun fail(job: QuestionCreationJob, exception: Throwable, canRetry: Boolean)
    suspend fun complete(job: QuestionCreationJob, quizId: QuizId)
    suspend fun requeue(job: QuestionCreationJob)
}

@Component
class JobProgressReporter(
    private val jobService: QuestionCreationJobService,
    private val jobUpdatePublisher: JobUpdatePublisher
) : ProgressReporter {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun update(job: QuestionCreationJob, progress: Int) =
        report(job) { markRunning(progress) }

    override suspend fun fail(
        job: QuestionCreationJob,
        exception: Throwable,
        canRetry: Boolean
    ) = report(job) { markFailed(exception.message.toString(), canRetry) }

    override suspend fun complete(job: QuestionCreationJob, quizId: QuizId) =
        report(job) { markCompleted(quizId) }

    override suspend fun requeue(job: QuestionCreationJob) =
        report(job) { markQueued() }

    private suspend fun report(
        job: QuestionCreationJob,
        transition: QuestionCreationJob.() -> QuestionCreationJob
    ) {
        outcomeOf {
            val updatedJob = transition(job)

            jobService.save(updatedJob)
            jobUpdatePublisher.publish(updatedJob.id)
        }.onFailure { error ->
            logger.error(
                "Failed to report job state change for job ID: ${job.id}: ${error.message}",
                error.exception
            )
        }
    }
}
