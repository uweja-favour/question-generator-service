@file:OptIn(ExperimentalTime::class)

package com.xapps.question_generator.job.persistence.entity.question_creation_job.factory

import com.xapps.question_generator.job.persistence.entity.JobStatusType
import com.xapps.question_generator.job.persistence.entity.QuestionCreationJobDocument
import com.xapps.dto.job.JobStatus
import com.xapps.dto.job.JobTaskCode
import com.xapps.question_generator.job.persistence.entity.QuestionGenerationSpecDocument
import com.xapps.time.types.KotlinInstant
import kotlin.time.ExperimentalTime

object QuestionCreationJobDocumentFactory {

    fun create(
        jobId: String,
        questionGenerationSpec: QuestionGenerationSpecDocument,
        jobStatus: JobStatus,
        attemptCount: Int,
        jobTaskCode: JobTaskCode,
        createdAt: KotlinInstant,
        updatedAt: KotlinInstant
    ): QuestionCreationJobDocument {

        return when (jobStatus) {

            JobStatus.Queued ->
                QuestionCreationJobDocument(
                    jobId = jobId,
                    questionGenerationSpec = questionGenerationSpec,
                    jobStatusTypeCode = JobStatusType.QUEUED.code,
                    jobTaskCode = jobTaskCode,
                    quizId = null,
                    progress = null,
                    failureReason = null,
                    canRetry = null,
                    attemptCount = attemptCount,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )

            is JobStatus.Running -> {
                require(jobStatus.percentage in 1..100) {
                    "JobStatus percentage must be in the range of 1..100. It was ${jobStatus.percentage}%"
                }

                QuestionCreationJobDocument(
                    jobId = jobId,
                    questionGenerationSpec = questionGenerationSpec,
                    jobStatusTypeCode = JobStatusType.RUNNING.code,
                    quizId = null,
                    progress = jobStatus.percentage,
                    failureReason = null,
                    canRetry = null,
                    attemptCount = attemptCount,
                    jobTaskCode = jobTaskCode,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            }

            is JobStatus.Completed ->
                QuestionCreationJobDocument(
                    jobId = jobId,
                    questionGenerationSpec = questionGenerationSpec,
                    jobStatusTypeCode = JobStatusType.COMPLETED.code,
                    quizId = jobStatus.quizId,
                    progress = null,
                    failureReason = null,
                    canRetry = null,
                    attemptCount = attemptCount,
                    jobTaskCode = jobTaskCode,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )

            is JobStatus.Failed ->
                QuestionCreationJobDocument(
                    jobId = jobId,
                    questionGenerationSpec = questionGenerationSpec,
                    jobStatusTypeCode = JobStatusType.FAILED.code,
                    quizId = null,
                    progress = null,
                    failureReason = jobStatus.reason,
                    canRetry = jobStatus.canRetry,
                    attemptCount = attemptCount,
                    jobTaskCode = jobTaskCode,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )

            JobStatus.Cancelled ->
                QuestionCreationJobDocument(
                    jobId = jobId,
                    questionGenerationSpec = questionGenerationSpec,
                    jobStatusTypeCode = JobStatusType.CANCELLED.code,
                    quizId = null,
                    progress = null,
                    failureReason = null,
                    canRetry = null,
                    attemptCount = attemptCount,
                    jobTaskCode = jobTaskCode,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
        }
    }
}
