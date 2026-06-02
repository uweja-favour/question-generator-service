package com.xapps.question_generator.question_generation_workflow.question_generator

import com.xapps.question_generator.infrastructure.object_store.ExtractableFile
import com.xapps.question_generator.job.JobProgressReporter
import com.xapps.question_generator.question_generation_workflow.pipeline.QuestionGenerator
import com.xapps.question_generator.question_generation_workflow.throwJobNotFoundException
import com.xapps.question_generation.dto.QuestionDTO
import com.xapps.question_generation.QuestionGenerationSpec
import com.xapps.question_generator.job.domain.repository.QuestionCreationJobRepository
import org.springframework.stereotype.Component

@Component
class QuestionGeneratorImpl(
    private val jobRepository: QuestionCreationJobRepository,
    private val contentPreparer: ContentPreparer,
    private val optimizer: NoteOptimizationService,
    private val allocationGenerator: AllocationQuestionGenerator,
    private val reporter: JobProgressReporter
) : QuestionGenerator {

    override suspend fun generate(
        spec: QuestionGenerationSpec,
        files: List<ExtractableFile>
    ): List<QuestionDTO> {

        val job = jobRepository.findById(spec.jobId)
            ?: throwJobNotFoundException(spec.jobId)

        reporter.update(job, Progress.QUESTIONS_GENERATION_STARTED)

        val rawContent = contentPreparer.prepare(files)

//        val optimizedContent = optimizer.optimize(job, rawContent)
//        ensureActive()

        return allocationGenerator.generate(job, rawContent, spec.allocations)
    }
}