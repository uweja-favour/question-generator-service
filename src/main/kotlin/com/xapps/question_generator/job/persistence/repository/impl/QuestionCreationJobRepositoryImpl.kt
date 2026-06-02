package com.xapps.question_generator.job.persistence.repository.impl

import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import com.xapps.question_generator.job.domain.repository.QuestionCreationJobRepository
import com.xapps.question_generator.job.persistence.mapper.toDomain
import com.xapps.question_generator.job.persistence.mapper.toEntity
import com.xapps.question_generator.saveUpserting
import com.xapps.question_generation.JobId
import com.xapps.question_generator.job.persistence.repository.QuestionCreationJobMongoRepository
import com.xapps.question_generator.saveAllUpserting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component

@Component
class QuestionCreationJobRepositoryImpl(
    private val repository: QuestionCreationJobMongoRepository,
) : QuestionCreationJobRepository {

    override suspend fun save(job: QuestionCreationJob): QuestionCreationJob {
        repository.saveUpserting(job.toEntity())
        return job
    }

    override suspend fun saveAll(jobs: List<QuestionCreationJob>): List<QuestionCreationJob> {
        repository.saveAllUpserting(jobs.map { it.toEntity() })
        return jobs
    }

    override suspend fun findById(id: JobId): QuestionCreationJob? =
        run {
            val jobEntity = repository.findById(id.value)
                ?: return@run null

            jobEntity.toDomain()
        }

    override suspend fun findAllByIds(ids: List<JobId>): List<QuestionCreationJob> {
        return ids.mapNotNull { jobId -> findById(jobId) }
    }

    override fun observe(jobs: Set<JobId>): Flow<QuestionCreationJob> {
        return repository.findAllById(
            jobs.map { it.value }.asIterable()
        ).map { it.toDomain() }
    }
}