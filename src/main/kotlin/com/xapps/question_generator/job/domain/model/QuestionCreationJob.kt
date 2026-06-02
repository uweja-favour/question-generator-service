@file:OptIn(ExperimentalTime::class)

package com.xapps.question_generator.job.domain.model

import com.xapps.platform.core.time.nowInKotlinInstant
import com.xapps.time.types.KotlinInstant
import com.xapps.dto.job.JobStatus
import com.xapps.dto.job.JobTask
import com.xapps.question_generation.JobId
import com.xapps.question_generation.QuestionGenerationSpec
import kotlin.time.ExperimentalTime

data class QuestionCreationJob(
    override val id: JobId,
    override val status: JobStatus,
    override val task: JobTask,
    override val createdAt: KotlinInstant,
    override val updatedAt: KotlinInstant,
    override val attemptCount: Int,
    val questionGenerationSpec: QuestionGenerationSpec
) : CreationJob() {

    override fun markRunning(progress: Int) = copy(
        status = JobStatus.Running(progress),
        updatedAt = nowInKotlinInstant()
    )

    override fun markCompleted() = copy(
        status = JobStatus.Completed,
        updatedAt = nowInKotlinInstant()
    )

    override fun markFailed(reason: String, canRetry: Boolean) = copy(
        status = JobStatus.Failed(
            reason = reason,
            canRetry = canRetry
        ),
        updatedAt = nowInKotlinInstant()
    )

    override fun markQueued() = copy(
        status = JobStatus.Queued,
        updatedAt = nowInKotlinInstant()
    )

    override fun incrementAttempt() = copy(
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
