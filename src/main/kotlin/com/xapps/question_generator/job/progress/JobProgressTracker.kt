package com.xapps.question_generator.job.progress

import com.xapps.dto.job.isFinal
import com.xapps.questions.contracts.question_generation.JobId
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

interface JobProgressTracker {
    fun snapshots(): Flow<JobSnapshot>
}

private typealias UpdateCollector = ProducerScope<JobSnapshot>

class PollingJobProgressTracker(
    private val snapshotProvider: JobSnapshotProvider,
    jobIds: Set<JobId>
) : JobProgressTracker {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val initialJobs = jobIds.toSet()

    override fun snapshots(): Flow<JobSnapshot> = flow {
        val activeJobs = initialJobs.toMutableSet()
        val deadline = System.currentTimeMillis() + maxDuration.inWholeMilliseconds

        while (activeJobs.isNotEmpty() && System.currentTimeMillis() < deadline) {
            delay(pollInterval)

            val snapshots = snapshotProvider.load(activeJobs)
            for (snapshot in snapshots) {
                emit(snapshot)

                if (snapshot.status.isFinal()) {
                    activeJobs.remove(snapshot.jobId)
                }
            }
        }
    }

    private companion object {
        val pollInterval: Duration = 4.seconds
        val maxDuration: Duration = 30.minutes
    }
}
