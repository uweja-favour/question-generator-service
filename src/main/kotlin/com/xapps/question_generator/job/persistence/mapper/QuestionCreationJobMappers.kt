@file:OptIn(ExperimentalTime::class)

package com.xapps.question_generator.job.persistence.mapper

import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import com.xapps.question_generator.job.persistence.entity.QuestionCreationJobDocument
import com.xapps.dto.job.JobTask
import com.xapps.questions.contracts.question_generation.JobId
import kotlin.time.ExperimentalTime

fun QuestionCreationJobDocument.toDomain(): QuestionCreationJob {

    val jobTask = requireNotNull(JobTask.fromCodeOrNull(taskCode)) {
        "Unknown job task code: $taskCode"
    }

    return QuestionCreationJob(
        id = JobId.of(jobId),
        status = status,
        task = jobTask,
        createdAt = createdAt,
        updatedAt = updatedAt,
        attemptCount = attemptCount,
        questionGenerationSpec = questionGenerationSpec.toDomain()
    )
}

fun QuestionCreationJob.toEntity(): QuestionCreationJobDocument {
    return QuestionCreationJobDocument(
        jobId = id.value,
        questionGenerationSpec = questionGenerationSpec.toEntity(),
        status = status,
        attemptCount = attemptCount,
        taskCode = task.code,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

