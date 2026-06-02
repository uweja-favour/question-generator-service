package com.xapps.question_generator.job.domain.model

import com.xapps.dto.job.JobStatus
import com.xapps.dto.job.JobTask
import com.xapps.question_generation.JobId
import com.xapps.time.types.KotlinInstant

sealed class CreationJob {
    abstract val id: JobId
    abstract val status: JobStatus
    abstract val task: JobTask
    abstract val createdAt: KotlinInstant
    abstract val updatedAt: KotlinInstant 
    abstract val attemptCount: Int


    abstract fun markRunning(progress: Int): CreationJob
    abstract fun markCompleted(): CreationJob
    abstract fun markFailed(reason: String, canRetry: Boolean): CreationJob
    abstract fun markQueued(): CreationJob
    abstract fun incrementAttempt(): CreationJob

}
