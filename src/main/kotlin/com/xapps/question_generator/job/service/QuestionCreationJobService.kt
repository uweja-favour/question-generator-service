package com.xapps.question_generator.job.service

import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import com.xapps.question_generator.job.domain.repository.QuestionCreationJobRepository
import com.xapps.dto.job.JobTask
import com.xapps.question_generator.workflow.JobProcessor
import com.xapps.questions.contracts.question_generation.JobId
import com.xapps.questions.contracts.self_test_generation.model.QuestionGenerationSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

interface QuestionCreationJobService {
    suspend fun createAndEnqueueJob(
        jobId: JobId,
        task: JobTask,
        spec: QuestionGenerationSpec
    )
    suspend fun findById(id: JobId): QuestionCreationJob?
    suspend fun findAllByIds(ids: List<JobId>): List<QuestionCreationJob>
    suspend fun save(job: QuestionCreationJob)
}

@Service
class QuestionCreationJobServiceImpl(
    private val rabbitTemplate: RabbitTemplate,
    private val jobRepository: QuestionCreationJobRepository
) : QuestionCreationJobService {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun createAndEnqueueJob(
        jobId: JobId,
        task: JobTask,
        spec: QuestionGenerationSpec
    ) {
        log.info("About to launch questions generation.")
        save(QuestionCreationJob.new(jobId, task, spec))
//        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, jobId.value)

        log.info("Successfully scheduled questions generation to rabbit.")
    }

    override suspend fun save(job: QuestionCreationJob) =
        jobRepository.save(job)

    override suspend fun findById(id: JobId): QuestionCreationJob? =
        jobRepository.findById(id) ?: run {
            log.error("Job with ID=${id.value} COULD NOT BE FOUND."); null
        }

    override suspend fun findAllByIds(ids: List<JobId>): List<QuestionCreationJob> =
        jobRepository.findAllById(ids)
}
