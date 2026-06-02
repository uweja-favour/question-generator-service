package com.xapps.dto

import com.xapps.question_generation.JobId
import kotlinx.serialization.Serializable

@Serializable
data class FetchJobRequest(
    val jobId: JobId
)