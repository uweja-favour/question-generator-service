package com.xapps.question_generator.job.persistence.mapper

import com.xapps.dto.job.JobTask
import com.xapps.question_generation.JobId
import com.xapps.question_generator.job.domain.model.NoteSummaryCreationJob
import com.xapps.question_generator.job.persistence.entity.NoteSummaryCreationJobDocument

fun NoteSummaryCreationJob.toEntity(): NoteSummaryCreationJobDocument {
    return NoteSummaryCreationJobDocument(
        jobId = id.value,
        noteSummaryGenerationSpec = noteSummaryGenerationSpec.toEntity(),
        status = status,
        attemptCount = attemptCount,
        taskCode = task.code,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun NoteSummaryCreationJobDocument.toDomain(): NoteSummaryCreationJob {
    return NoteSummaryCreationJob(
        id = JobId.of(jobId),
        status = status,
        task = JobTask.fromCodeOrNull(taskCode) ?: error("Invalid job task code: $taskCode"),
        createdAt = createdAt,
        updatedAt = updatedAt,
        attemptCount = attemptCount,
        noteSummaryGenerationSpec = noteSummaryGenerationSpec.toDomain()
    )
}