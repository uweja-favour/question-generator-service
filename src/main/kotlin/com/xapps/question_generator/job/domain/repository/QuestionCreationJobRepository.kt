package com.xapps.question_generator.job.domain.repository

import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import com.xapps.questions.contracts.question_generation.JobId
import kotlinx.coroutines.flow.Flow

interface QuestionCreationJobRepository {
    suspend fun save(job: QuestionCreationJob)
    suspend fun findById(jobId: JobId): QuestionCreationJob?
    suspend fun findAllById(jobIds: List<JobId>): List<QuestionCreationJob>
    fun observe(jobs: Set<JobId>): Flow<QuestionCreationJob>
}