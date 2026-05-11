package com.xapps.question_generator.workflow.question_generator

import com.xapps.question_generator.gpt_service.GptClient
import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import com.xapps.question_generator.prompt_builder.NoteOptimizerPromptBuilder
import com.xapps.question_generator.job.JobProgressReporter
import org.springframework.stereotype.Service

interface NoteOptimizationService {
    suspend fun optimize(job: QuestionCreationJob, content: String): String
}

@Service
class NoteOptimizationServiceImpl(
    private val gptClient: GptClient,
    private val promptBuilder: NoteOptimizerPromptBuilder,
    private val reporter: JobProgressReporter
) : NoteOptimizationService {

    override suspend fun optimize(job: QuestionCreationJob, content: String): String {
        reporter.update(job, Progress.OPTIMIZATION_STARTED)

        val prompt = promptBuilder.buildOptimizationPrompt(content)

        val optimized = gptClient.generateStringResponse(prompt, allowGpt5 = true)

        reporter.update(job, Progress.OPTIMIZATION_COMPLETED)

        return optimized
    }
}