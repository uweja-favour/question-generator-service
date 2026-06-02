package com.xapps.question_generator.job.service

import com.xapps.question_generation.JobId
import com.xapps.question_generator.job.domain.model.CreationJob
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

interface CreationJobService {
    suspend fun save(job: CreationJob): CreationJob
    suspend fun saveAll(jobs: List<CreationJob>): List<CreationJob>
    suspend fun findById(id: JobId): CreationJob?
    suspend fun findAllByIds(ids: List<JobId>): List<CreationJob>
}

@Service
class CreationJobServiceImpl(
    private val repositoryRegistry: CreationJobRepositoryRegistry
) : CreationJobService {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun save(job: CreationJob): CreationJob {
        val repository = repositoryFor(job)

        return repository.save(job)
    }

    override suspend fun saveAll(jobs: List<CreationJob>): List<CreationJob> {
        if (jobs.isEmpty()) {
            return emptyList()
        }

        val jobsGroupedByType = jobs.groupBy { it::class }

        return buildList {
            jobsGroupedByType.forEach { (type, groupedJobs) ->

                val repository = repositoryRegistry.repositoryFor(type)

                addAll(
                    repository.saveAll(groupedJobs)
                )
            }
        }
    }

    override suspend fun findById(id: JobId): CreationJob? {
        repositoryRegistry
            .allRepositories()
            .forEach { repository ->

                val result = findInRepository(repository, id)

                if (result != null) {
                    return result
                }
            }

        return null
    }

    override suspend fun findAllByIds(
        ids: List<JobId>
    ): List<CreationJob> {

        return buildList {
            repositoryRegistry
                .allRepositories()
                .forEach { repository ->

                    addAll(
                        findAllInRepository(repository, ids)
                    )
                }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun repositoryFor(
        job: CreationJob
    ): CreationJobRepository<CreationJob> {

        return repositoryRegistry
            .repositoryFor(
                job::class as KClass<CreationJob>
            )
    }

    private suspend fun findInRepository(
        repository: CreationJobRepository<out CreationJob>,
        id: JobId
    ): CreationJob? {

        @Suppress("UNCHECKED_CAST")
        return (repository as CreationJobRepository<CreationJob>)
            .findById(id)
    }

    private suspend fun findAllInRepository(
        repository: CreationJobRepository<out CreationJob>,
        ids: List<JobId>
    ): List<CreationJob> {

        @Suppress("UNCHECKED_CAST")
        return (repository as CreationJobRepository<CreationJob>)
            .findAllByIds(ids)
    }
}