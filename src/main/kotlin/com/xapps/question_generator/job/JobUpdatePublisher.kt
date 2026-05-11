package com.xapps.question_generator.job

import com.xapps.questions.contracts.question_generation.JobId
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class JobUpdatePublisher(
    private val redis: StringRedisTemplate
) {

    companion object {
        const val JOB_UPDATES = "job-updates"
    }

    fun publish(jobId: JobId) {
//        redis.convertAndSend(JOB_UPDATES, jobId.value)
    }
}
