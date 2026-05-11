package com.xapps.question_generator.job.persistence.entity

@JvmInline
value class JobStatusTypeCode(val value: String)

enum class JobStatusType(val code: JobStatusTypeCode, val displayName: String) {
    QUEUED(JobStatusTypeCode("queued"), "Queued"),
    RUNNING(JobStatusTypeCode("running"), "Running"),
    COMPLETED(JobStatusTypeCode("completed"), "Completed"),
    FAILED(JobStatusTypeCode("failed"), "Failed"),
    CANCELLED(JobStatusTypeCode("cancelled"), "Cancelled");

    companion object {
        private val BY_CODE = entries.associateBy { it.code }

        fun fromCodeOrNull(code: JobStatusTypeCode): JobStatusType? {
            return BY_CODE[code]
        }
    }
}