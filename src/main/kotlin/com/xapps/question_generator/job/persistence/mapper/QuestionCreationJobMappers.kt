@file:OptIn(ExperimentalTime::class)

package com.xapps.question_generator.job.persistence.mapper

import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import com.xapps.question_generator.job.persistence.entity.JobStatusType
import com.xapps.question_generator.job.persistence.entity.QuestionCreationJobDocument
import com.xapps.question_generator.job.persistence.entity.question_creation_job.factory.QuestionCreationJobDocumentFactory
import com.xapps.dto.job.JobStatus
import com.xapps.dto.job.JobTask
import com.xapps.questions.contracts.question_generation.JobId
import kotlin.time.ExperimentalTime

fun QuestionCreationJobDocument.toDomain(): QuestionCreationJob {

    val jobStatus = requireNotNull(JobStatusType.fromCodeOrNull(jobStatusTypeCode)) {
        "Unknown job status type: $jobStatusTypeCode"
    }

    val jobTask = requireNotNull(JobTask.fromCodeOrNull(jobTaskCode)) {
        "Unknown job task code: $jobTaskCode"
    }

    val status = when (jobStatus) {
        JobStatusType.QUEUED ->
            JobStatus.Queued

        JobStatusType.RUNNING ->
            JobStatus.Running(
                percentage = requireNotNull(progress)
            )

        JobStatusType.COMPLETED ->
            JobStatus.Completed(
                quizId = requireNotNull(quizId)
            )

        JobStatusType.FAILED ->
            JobStatus.Failed(
                reason = requireNotNull(failureReason),
                canRetry = requireNotNull(canRetry)
            )

        JobStatusType.CANCELLED ->
            JobStatus.Cancelled
    }


    return QuestionCreationJob(
        id = JobId.of(this.jobId),
        status = status,
        task = jobTask,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        attemptCount = this.attemptCount,
        questionGenerationSpec = questionGenerationSpec.toDomain()
    )
}

fun QuestionCreationJob.toEntity(): QuestionCreationJobDocument {
    return QuestionCreationJobDocumentFactory.create(
        jobId = id.value,
        questionGenerationSpec = questionGenerationSpec.toEntity(),
        jobStatus = status,
        attemptCount = attemptCount,
        jobTaskCode = task.code,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}