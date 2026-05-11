package com.xapps.question_generator.job.progress

import com.xapps.dto.job.isFinal
import com.xapps.questions.contracts.question_generation.JobId
import kotlin.collections.toMutableSet

internal class ActiveJobSet(
    initialJobs: Set<JobId>
) {

    private val activeJobs = initialJobs.toMutableSet()

    fun onUpdate(update: JobSnapshot) {
        if (update.status.isFinal()) {
            activeJobs.remove(update.jobId)
        }
    }

    fun hasActiveJobs(): Boolean = activeJobs.isNotEmpty()

    fun snapshot(): Set<JobId> = activeJobs.toSet()
}
