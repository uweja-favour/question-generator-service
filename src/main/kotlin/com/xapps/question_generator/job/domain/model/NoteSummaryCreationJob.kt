package com.xapps.question_generator.job.domain.model

import com.xapps.dto.job.JobStatus
import com.xapps.dto.job.JobTask
import com.xapps.note_summary.NoteSummaryGenerationSpec
import com.xapps.platform.core.time.nowInKotlinInstant
import com.xapps.question_generation.JobId
import com.xapps.time.types.KotlinInstant
import org.springframework.data.mongodb.core.query.update

data class NoteSummaryCreationJob(
    override val id: JobId,
    override val status: JobStatus,
    override val task: JobTask,
    override val createdAt: KotlinInstant,
    override val updatedAt: KotlinInstant,
    override val attemptCount: Int,
    val noteSummaryGenerationSpec: NoteSummaryGenerationSpec
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
            spec: NoteSummaryGenerationSpec
        ): NoteSummaryCreationJob {
            val now = nowInKotlinInstant()
            return NoteSummaryCreationJob(
                id = jobId,
                status = JobStatus.Queued,
                task = JobTask.NOTE_SUMMARY,
                createdAt = now,
                updatedAt = now,
                attemptCount = 0,
                noteSummaryGenerationSpec = spec
            )
        }
    }
}