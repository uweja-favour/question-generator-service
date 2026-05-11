@file:OptIn(ExperimentalTime::class)

package com.xapps.question_generator.job.domain.model

import com.xapps.platform.core.time.nowInKotlinInstant
import com.xapps.time.types.KotlinInstant
import com.xapps.dto.job.JobStatus
import com.xapps.dto.job.JobTask
import com.xapps.model.QuizId
import com.xapps.questions.contracts.question_generation.JobId
import com.xapps.questions.contracts.self_test_generation.model.QuestionGenerationSpec
import kotlin.time.ExperimentalTime

data class QuestionCreationJob(
    val id: JobId,
    val status: JobStatus,
    val task: JobTask,
    val createdAt: KotlinInstant,
    val updatedAt: KotlinInstant,
    val attemptCount: Int,
    val questionGenerationSpec: QuestionGenerationSpec
) {
    fun markRunning(progress: Int) = copy(
        status = JobStatus.Running(progress),
        updatedAt = nowInKotlinInstant(),
    )

    fun markCompleted(quizId: QuizId) = copy(
        status = JobStatus.Completed(quizId),
        updatedAt = nowInKotlinInstant()
    )

    fun markFailed(reason: String, canRetry: Boolean) = copy(
        status = JobStatus.Failed(
            reason = reason,
            canRetry = canRetry
        ),
        updatedAt = nowInKotlinInstant()
    )

    fun markQueued() = copy(
        status = JobStatus.Queued,
        updatedAt = nowInKotlinInstant()
    )

    fun shouldRun(): Boolean =
        status is JobStatus.Queued ||
        status is JobStatus.Failed && status.canRetry


    fun incrementAttempt() = copy(
        attemptCount = attemptCount + 1,
        updatedAt = nowInKotlinInstant()
    )

    fun toReadableFormat(): String = "JobID=$id | status=$status | " +
            "updatedAt=$updatedAt | attempts=$attemptCount"

    companion object {
        fun new(
            jobId: JobId,
            task: JobTask,
            spec: QuestionGenerationSpec
        ): QuestionCreationJob {
            return QuestionCreationJob(
                id = jobId,
                status = JobStatus.Queued,
                task = task,
                createdAt = nowInKotlinInstant(),
                updatedAt = nowInKotlinInstant(),
                attemptCount = 0,
                questionGenerationSpec = spec
            )
        }
    }
}
