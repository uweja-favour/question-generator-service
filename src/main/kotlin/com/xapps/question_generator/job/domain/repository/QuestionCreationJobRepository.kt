package com.xapps.question_generator.job.domain.repository

import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import com.xapps.question_generation.JobId
import com.xapps.question_generator.job.service.CreationJobRepository
import kotlinx.coroutines.flow.Flow

interface QuestionCreationJobRepository : CreationJobRepository<QuestionCreationJob> {
    fun observe(jobs: Set<JobId>): Flow<QuestionCreationJob>
}