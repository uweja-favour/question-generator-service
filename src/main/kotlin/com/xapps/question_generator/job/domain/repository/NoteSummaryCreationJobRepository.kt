package com.xapps.question_generator.job.domain.repository

import com.xapps.question_generation.JobId
import com.xapps.question_generator.job.domain.model.NoteSummaryCreationJob
import com.xapps.question_generator.job.service.CreationJobRepository
import kotlinx.coroutines.flow.Flow

interface NoteSummaryCreationJobRepository : CreationJobRepository<NoteSummaryCreationJob> {
    fun observe(jobs: Set<JobId>): Flow<NoteSummaryCreationJob>
}