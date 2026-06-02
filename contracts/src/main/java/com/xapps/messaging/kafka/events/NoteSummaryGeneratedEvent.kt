package com.xapps.messaging.kafka.events

import com.xapps.model.NoteSummaryStyle
import com.xapps.question_generation.JobId
import kotlinx.serialization.Serializable

@Serializable
data class NoteSummaryGeneratedEvent(
    val noteSummaryId: String,
    val userId: String,
    val content: String
)

@Serializable
data class NoteSummaryRequestedEvent(
    val noteSummaryId: String,
    val userId: String,
    val jobId: JobId,
    val fileKey: String,
    val style: NoteSummaryStyle
)