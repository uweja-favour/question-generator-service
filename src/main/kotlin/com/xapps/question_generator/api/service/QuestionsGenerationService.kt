package com.xapps.question_generator.api.service

import com.xapps.platform.core.retryWithExponentialBackoff
import com.xapps.platform.core.string.generateUniqueId
import com.xapps.dto.job.JobDTO
import com.xapps.dto.job.JobTask
import com.xapps.model.QuizType
import com.xapps.question_generation.JobId
import com.xapps.dto.QuestionAllocationDTO
import com.xapps.model.NoteSummaryStyle
import com.xapps.model.QuizId
import com.xapps.note_summary.createNoteSummaryGenerationSpec
import com.xapps.question_generation.createQuestionGenerationSpec
import com.xapps.question_generator.infrastructure.object_store.ObjectKey
import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import com.xapps.question_generator.question_generation_workflow.QuestionProcessor
import com.xapps.question_generator.job.domain.model.NoteSummaryCreationJob
import com.xapps.question_generator.job.domain.repository.NoteSummaryCreationJobRepository
import com.xapps.question_generator.job.domain.repository.QuestionCreationJobRepository
import com.xapps.question_generator.job.service.CreationJobService
import com.xapps.question_generator.note_summary_workflow.NoteSummaryCreationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class QuestionsGenerationService(
    private val questionCreationJobRepository: QuestionCreationJobRepository,
    private val noteSummaryCreationJobRepository: NoteSummaryCreationJobRepository,
    private val jobService: CreationJobService,
    private val questionProcessor: QuestionProcessor,
    private val noteSummaryCreation: NoteSummaryCreationService
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
        val spec = createQuestionGenerationSpec(
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
        questionCreationJobRepository.save(job)

        log.info("Starting questions generation now.")
        scope.launch { questionProcessor.run(jobId) }
    }

    suspend fun generateNoteSummary(
        noteSummaryId: String,
        userId: String,
        jobId: JobId,
        fileKey: String,
        style: NoteSummaryStyle
    ) {

        val spec = createNoteSummaryGenerationSpec(
            noteSummaryId = noteSummaryId,
            userId = userId,
            fileKey = fileKey,
            style = style
        )

        val job = NoteSummaryCreationJob.new(jobId, spec)
        noteSummaryCreationJobRepository.save(job)

        log.info("Starting note summary generation now.")
        scope.launch { noteSummaryCreation.run(jobId) }
    }

    suspend fun fetchJob(jobId: JobId): JobDTO {
        val job = retryWithExponentialBackoff { jobService.findById(jobId) }
        return JobDTO(job.id, job.task, job.status)
    }
}

