package com.xapps.question_generator.job.progress

import com.xapps.question_generation.JobId
import com.xapps.question_generator.job.domain.repository.QuestionCreationJobRepository
import org.springframework.stereotype.Service

interface JobSnapshotProvider {
    suspend fun load(jobIds: Set<JobId>): List<JobSnapshot>
}

@Service
class QuizJobSnapshotProvider(
    private val jobRepository: QuestionCreationJobRepository
) : JobSnapshotProvider {

    override suspend fun load(jobIds: Set<JobId>): List<JobSnapshot> =
        jobRepository.findAllByIds(jobIds.toList())
            .map {
                JobSnapshot(
                    jobId = it.id,
                    status = it.status
                )
            }
}