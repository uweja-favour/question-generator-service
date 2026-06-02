package com.xapps.question_generator.note_summary_workflow

import com.xapps.question_generation.JobId
import com.xapps.question_generator.claude_service.ClaudeService
import com.xapps.question_generator.file_text_extractor.FileTextExtractorService
import com.xapps.question_generator.infrastructure.object_store.asExtractableFile
import com.xapps.question_generator.infrastructure.resilience4j.RetryExecutor
import com.xapps.question_generator.job.JobProgressReporter
import com.xapps.question_generator.job.domain.repository.NoteSummaryCreationJobRepository
import com.xapps.question_generator.note_summary_workflow.ai_text_cleaner.AiTextCleaner
import com.xapps.question_generator.note_summary_workflow.prompt_builder.NoteSummaryPromptBuilderService
import com.xapps.question_generator.question_generation_workflow.pipeline.FileProcessor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NoteSummaryCreationService(
    private val fileProcessor: FileProcessor,
    private val fileTextExtractorService: FileTextExtractorService,
    private val promptBuilder: NoteSummaryPromptBuilderService,
    private val claudeService: ClaudeService,
    private val publisher: NoteSummaryPublisher,
    private val retryExecutor: RetryExecutor,
    private val jobService: NoteSummaryCreationJobRepository,
    private val reporter: JobProgressReporter
) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun run(jobId: JobId) {
        try {
            retryExecutor.execute {

                val job = jobService.findById(jobId)
                    ?: run {
                        log.error("Job $jobId does not exist")
                        return@execute
                    }

                val updatedJob = job.incrementAttempt()
                jobService.save(updatedJob)

                val spec = job.noteSummaryGenerationSpec

                val handle = fileProcessor.fetch(spec.fileKey)
                val extractedText = fileTextExtractorService.extractText(handle.asExtractableFile()).value

                reporter.update(updatedJob, 50)

                val prompt = promptBuilder.buildPrompt(
                    note = extractedText,
                    style = spec.style
                )

                val content = claudeService.generateStringResponse(
                    prompt,
                ).let {
                    AiTextCleaner.clean(it)
                }

                reporter.update(updatedJob, 80)

                publisher.publishNoteSummary(content, spec)

                reporter.complete(updatedJob)
            }
        } catch (ex: Exception) {
            log.error("Job failed after retries: ${jobId.value}", ex)

            jobService.findById(jobId)?.let {
                reporter.fail(it, ex, canRetry = false)
            }
        }
    }
}