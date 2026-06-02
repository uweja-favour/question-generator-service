package com.xapps.question_generator.job.persistence.entity

import com.xapps.dto.job.JobStatus
import com.xapps.dto.job.JobTaskCode
import com.xapps.question_generator.BasePersistableEntity
import com.xapps.time.types.KotlinInstant
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "note_summary_creation_jobs")
data class NoteSummaryCreationJobDocument(
    @Id
    val jobId: String,

    val noteSummaryGenerationSpec: NoteSummaryGenerationSpecDocument,

    val status: JobStatus,

    val attemptCount: Int,

    val taskCode: JobTaskCode,

    val createdAt: KotlinInstant,

    val updatedAt: KotlinInstant
) : BasePersistableEntity() {

    override fun getTheId(): String = jobId
}

