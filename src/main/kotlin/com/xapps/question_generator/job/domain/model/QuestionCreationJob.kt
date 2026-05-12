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
        updatedAt = nowInKotlinInstant()
    )

    fun markCompleted() = copy(
        status = JobStatus.Completed,
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

    fun incrementAttempt() = copy(
        attemptCount = attemptCount + 1,
        updatedAt = nowInKotlinInstant()
    )

    companion object {
        fun new(
            jobId: JobId,
            task: JobTask,
            spec: QuestionGenerationSpec
        ): QuestionCreationJob {
            val now = nowInKotlinInstant()

            return QuestionCreationJob(
                id = jobId,
                status = JobStatus.Queued,
                task = task,
                createdAt = now,
                updatedAt = now,
                attemptCount = 0,
                questionGenerationSpec = spec
            )
        }
    }
}
