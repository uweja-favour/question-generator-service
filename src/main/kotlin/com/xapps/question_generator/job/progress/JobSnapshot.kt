package com.xapps.question_generator.job.progress

import com.xapps.dto.job.JobStatus
import com.xapps.dto.job.isFinal
import com.xapps.questions.contracts.question_generation.JobId

data class JobSnapshot(
    val jobId: JobId,
    val status: JobStatus
) {
    fun isFinal(): Boolean = status.isFinal()
}
