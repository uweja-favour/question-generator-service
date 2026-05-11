package com.xapps.question_generator.job.persistence.repository

import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import com.xapps.question_generator.job.domain.repository.QuestionCreationJobRepository
import com.xapps.question_generator.job.persistence.mapper.toDomain
import com.xapps.question_generator.job.persistence.mapper.toEntity
import com.xapps.question_generator.saveUpserting
import com.xapps.questions.contracts.question_generation.JobId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.springframework.stereotype.Component

@Component
class QuestionCreationJobRepositoryImpl(
    private val repository: QuestionCreationJobMongoRepository,
) : QuestionCreationJobRepository {

    override suspend fun save(job: QuestionCreationJob) {
        repository.saveUpserting(job.toEntity())
    }

    override suspend fun findById(jobId: JobId): QuestionCreationJob? =
        run {
            val jobEntity = repository.findById(jobId.value)
                ?: return@run null

            jobEntity.toDomain()
        }

    override suspend fun findAllById(jobIds: List<JobId>): List<QuestionCreationJob> {
        return jobIds.mapNotNull { jobId -> findById(jobId) }
    }

    override fun observe(jobs: Set<JobId>): Flow<QuestionCreationJob> {
        return repository.findAllById(
            jobs.map { it.value }.asIterable()
        ).mapNotNull { it.toDomain() }
    }
}