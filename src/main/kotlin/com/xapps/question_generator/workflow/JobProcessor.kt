package com.xapps.question_generator.workflow

import com.xapps.model.QuizType
import com.xapps.question_generator.infrastructure.object_store.asExtractableFile
import com.xapps.question_generator.infrastructure.resilience4j.RetryExecutor
import com.xapps.question_generator.job.JobProgressReporter
import com.xapps.question_generator.job.service.QuestionCreationJobService
import com.xapps.question_generator.workflow.pipeline.FileProcessor
import com.xapps.question_generator.workflow.pipeline.QuestionGenerator
import com.xapps.question_generator.workflow.question_generator.Progress
import com.xapps.questions.contracts.question_generation.JobId
import com.xapps.questions.contracts.self_test_generation.dto.QuestionDTO
import com.xapps.questions.contracts.self_test_generation.model.QuestionGenerationSpec
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class JobProcessor(
    private val jobService: QuestionCreationJobService,
    private val reporter: JobProgressReporter,
    private val fileProcessor: FileProcessor,
    private val generator: QuestionGenerator,
    private val retryExecutor: RetryExecutor,
    private val selfTestQuestionPublisher: SelfTestQuestionPublisher,
    private val classroomQuestionPublisher: ClassroomQuestionPublisher
) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun run(jobId: JobId) {

        val job = jobService.findById(jobId)
            ?.takeIf { it.shouldRun() }
            ?: run {
                log.error("Missing job with id: ${jobId.value}")
                return
            }

        try {
            retryExecutor.execute {

                val updatedJob = job.incrementAttempt()
                jobService.save(updatedJob)

                val spec = updatedJob.questionGenerationSpec

                val handles = fileProcessor.fetch(spec.fileKeys)
                val files = handles.map { it.asExtractableFile() }
                reporter.update(updatedJob, Progress.FETCHED_FILES)

                val questions = generator.generate(spec, files)
                reporter.update(updatedJob, Progress.QUESTIONS_GENERATION_COMPLETED)

                publishQuestions(questions, spec)

                reporter.complete(updatedJob, spec.quizId)
            }

        } catch (ex: Exception) {
            log.error("Job failed after retries: ${jobId.value}", ex)

            reporter.fail(job, ex, canRetry = false)
        }
    }

    private suspend fun publishQuestions(
        questions: List<QuestionDTO>,
        spec: QuestionGenerationSpec
    ) {
        when (spec.quizType) {
            QuizType.SELF_TEST ->
                selfTestQuestionPublisher.publishQuestions(questions, spec)

            QuizType.CLASSROOM ->
                classroomQuestionPublisher.publishQuestions(questions, spec)
        }
    }
}