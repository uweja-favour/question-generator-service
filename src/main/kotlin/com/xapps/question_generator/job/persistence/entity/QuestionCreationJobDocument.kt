package com.xapps.question_generator.job.persistence.entity

import com.xapps.dto.job.JobStatus
import com.xapps.dto.job.JobTaskCode
import com.xapps.question_generator.BasePersistableEntity
import com.xapps.time.types.KotlinInstant
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("question_creation_jobs")
data class QuestionCreationJobDocument(
    @Id
    val jobId: String,

    val questionGenerationSpec: QuestionGenerationSpecDocument,

    val status: JobStatus,

    val attemptCount: Int,

    val taskCode: JobTaskCode,

    val createdAt: KotlinInstant,

    val updatedAt: KotlinInstant
) : BasePersistableEntity() {

    override fun getTheId(): String = jobId
}


