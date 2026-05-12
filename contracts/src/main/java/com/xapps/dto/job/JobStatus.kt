package com.xapps.dto.job

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun JobStatus.isNotFinal() = !isFinal()

@Serializable
sealed class JobStatus {

    @Serializable
    @SerialName("Queued")
    data object Queued : JobStatus()

    @Serializable
    @SerialName("Running")
    data class Running(
        val progress: Int
    ) : JobStatus()

    @Serializable
    @SerialName("Completed")
    data object Completed : JobStatus()

    @Serializable
    @SerialName("Failed")
    data class Failed(
        val reason: String,
        val canRetry: Boolean
    ) : JobStatus()

    @Serializable
    @SerialName("Cancelled")
    data object Cancelled : JobStatus()
}

fun JobStatus.isFinal(): Boolean =
    this is JobStatus.Completed || (this is JobStatus.Failed && !canRetry) || this is JobStatus.Cancelled