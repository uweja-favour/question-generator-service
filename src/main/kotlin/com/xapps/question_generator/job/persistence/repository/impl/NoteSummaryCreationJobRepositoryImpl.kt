package com.xapps.question_generator.job.persistence.repository.impl

import com.xapps.question_generation.JobId
import com.xapps.question_generator.job.domain.model.NoteSummaryCreationJob
import com.xapps.question_generator.job.domain.repository.NoteSummaryCreationJobRepository
import com.xapps.question_generator.job.persistence.mapper.toDomain
import com.xapps.question_generator.job.persistence.mapper.toEntity
import com.xapps.question_generator.job.persistence.repository.NoteSummaryCreationJobMongoRepository
import com.xapps.question_generator.saveAllUpserting
import com.xapps.question_generator.saveUpserting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service

@Service
class NoteSummaryCreationJobRepositoryImpl(
    private val repository: NoteSummaryCreationJobMongoRepository
) : NoteSummaryCreationJobRepository {

    override suspend fun save(job: NoteSummaryCreationJob): NoteSummaryCreationJob {
        repository.saveUpserting(job.toEntity())
        return job
    }

    override suspend fun saveAll(jobs: List<NoteSummaryCreationJob>): List<NoteSummaryCreationJob> {
        repository.saveAllUpserting(jobs.map { it.toEntity() })
        return jobs
    }

    override suspend fun findById(id: JobId): NoteSummaryCreationJob? {
        return repository.findById(id.value)?.toDomain()
    }

    override suspend fun findAllByIds(ids: List<JobId>): List<NoteSummaryCreationJob> {
        return ids.mapNotNull { jobId -> findById(jobId) }
    }

    override fun observe(jobs: Set<JobId>): Flow<NoteSummaryCreationJob> {
        return repository.findAllById(
            jobs.map { it.value }.asIterable()
        ).map { it.toDomain() }
    }
}