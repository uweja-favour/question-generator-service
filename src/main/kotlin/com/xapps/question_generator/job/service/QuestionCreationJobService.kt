//package com.xapps.question_generator.job.service
//
//import com.xapps.question_generator.job.domain.model.QuestionCreationJob
//import com.xapps.question_generator.job.domain.repository.QuestionCreationJobRepository
//import com.xapps.dto.job.JobTask
//import com.xapps.question_generation.JobId
//import com.xapps.question_generation.QuestionGenerationSpec
//import com.xapps.question_generator.job.domain.model.CreationJob
//import com.xapps.question_generator.job.domain.model.NoteSummaryCreationJob
//import com.xapps.question_generator.job.domain.repository.NoteSummaryCreationJobRepository
//import org.slf4j.LoggerFactory
//import org.springframework.amqp.rabbit.core.RabbitTemplate
//import org.springframework.stereotype.Service
//
//interface QuestionCreationJobService {
//    suspend fun save(job: CreationJob)
//    suspend fun findById(id: JobId): NoteSummaryCreationJob?
//    suspend fun findById(id: JobId): QuestionCreationJob?
//    suspend fun findAllByIds(ids: List<JobId>): List<QuestionCreationJob>
//}
//
//@Service
//class QuestionCreationJobServiceImpl(
//    private val questionJobRepository: QuestionCreationJobRepository,
//    private val noteSummaryJobRepository: NoteSummaryCreationJobRepository
//) : QuestionCreationJobService {
//
//    private val log = LoggerFactory.getLogger(javaClass)
//
//    override suspend fun save(job: CreationJob) {
//        when(job) {
//            is QuestionCreationJob -> questionJobRepository.save(job)
//            is NoteSummaryCreationJob -> noteSummaryJobRepository.save(job)
//        }
//    }
//
//    override suspend fun findById(id: JobId): QuestionCreationJob? {
//
//    }
//
//    override suspend fun findAllByIds(ids: List<JobId>): List<QuestionCreationJob> =
//        jobRepository.findAllById(ids)
//}
