package com.xapps.question_generator.job.persistence.repository

import com.xapps.question_generator.job.persistence.entity.NoteSummaryCreationJobDocument
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NoteSummaryCreationJobMongoRepository : CoroutineCrudRepository<NoteSummaryCreationJobDocument, String> {
}