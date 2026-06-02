package com.xapps.question_generator.job.service

import com.xapps.question_generation.JobId
import com.xapps.question_generator.job.domain.model.CreationJob

interface CreationJobRepository<T : CreationJob> {

    suspend fun save(job: T): T

    suspend fun saveAll(jobs: List<T>): List<T>

    suspend fun findById(id: JobId): T?

    suspend fun findAllByIds(ids: List<JobId>): List<T>
}