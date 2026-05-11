package com.xapps.question_generator.job.progress

import com.xapps.question_generator.job.service.QuestionCreationJobService
import com.xapps.questions.contracts.question_generation.JobId
import org.springframework.stereotype.Service

interface JobSnapshotProvider {
    suspend fun load(jobIds: Set<JobId>): List<JobSnapshot>
}

@Service
class QuizJobSnapshotProvider(
    private val jobService: QuestionCreationJobService
) : JobSnapshotProvider {

    override suspend fun load(jobIds: Set<JobId>): List<JobSnapshot> =
        jobService.findAllByIds(jobIds.toList())
            .map {
                JobSnapshot(
                    jobId = it.id,
                    status = it.status
                )
            }
}