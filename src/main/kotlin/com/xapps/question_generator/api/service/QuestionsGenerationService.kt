package com.xapps.question_generator.api.service

import com.xapps.platform.core.retryWithExponentialBackoff
import com.xapps.platform.core.string.generateUniqueId
import com.xapps.question_generator.job.service.QuestionCreationJobService
import com.xapps.dto.job.JobDTO
import com.xapps.dto.job.JobTask
import com.xapps.model.QuizType
import com.xapps.questions.contracts.question_generation.JobId
import com.xapps.dto.QuestionAllocationDTO
import com.xapps.model.QuizId
import com.xapps.question_generator.infrastructure.object_store.ObjectKey
import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import com.xapps.question_generator.workflow.JobProcessor
import com.xapps.questions.contracts.self_test_generation.dto.createSelfTestQuestionGenerationSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class QuestionsGenerationService(
    private val jobService: QuestionCreationJobService,
    private val jobProcessor: JobProcessor
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun generateQuestions(
        userId: String,
        quizId: QuizId,
        jobId: JobId,
        questionCount: Int,
        fileKeys: List<ObjectKey>,
        allocations: List<QuestionAllocationDTO>,
        quizType: QuizType
    ) {
        val spec = createSelfTestQuestionGenerationSpec(
            userId = userId,
            quizId = quizId,
            questionCount = questionCount,
            allocations = allocations,
            fileKeys = fileKeys,
            jobId = jobId,
            quizType = quizType,
            idGenerator = ::generateUniqueId
        )

        val task = when(quizType) {
            QuizType.SELF_TEST -> JobTask.SELF_TEST
            QuizType.CLASSROOM -> JobTask.CLASSROOM
        }

        val job = QuestionCreationJob.new(jobId, task, spec)
        jobService.save(job)

        log.info("Starting questions generation now.")
        scope.launch { jobProcessor.run(jobId) }
    }

    suspend fun fetchJob(jobId: JobId): JobDTO {
        val job = retryWithExponentialBackoff { jobService.findById(jobId) }
        return JobDTO(job.id, job.task, job.status)
    }
}