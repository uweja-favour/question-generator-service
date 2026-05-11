package com.xapps.question_generator.job.progress

import com.xapps.dto.IdHolder
import com.xapps.questions.contracts.question_generation.JobId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service

@Service
class JobProgressSubscription(
    private val sseFactory: JobUpdateEventFactory,
    private val snapshotProvider: JobSnapshotProvider
) {

    fun subscribe(jobIds: Set<JobId>): Flow<ServerSentEvent<IdHolder>> =
        PollingJobProgressTracker(snapshotProvider, jobIds)
            .snapshots()
            .map(sseFactory::create)
}
