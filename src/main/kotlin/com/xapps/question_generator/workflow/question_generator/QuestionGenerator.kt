package com.xapps.question_generator.workflow.question_generator

import com.xapps.question_generator.infrastructure.object_store.ExtractableFile
import com.xapps.question_generator.job.service.QuestionCreationJobService
import com.xapps.question_generator.job.JobProgressReporter
import com.xapps.question_generator.workflow.pipeline.QuestionGenerator
import com.xapps.question_generator.workflow.throwJobNotFoundException
import com.xapps.questions.contracts.self_test_generation.dto.QuestionDTO
import com.xapps.questions.contracts.self_test_generation.model.QuestionGenerationSpec
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class QuestionGeneratorImpl(
    private val jobService: QuestionCreationJobService,
    private val contentPreparer: ContentPreparer,
    private val optimizer: NoteOptimizationService,
    private val allocationGenerator: AllocationQuestionGenerator,
    private val reporter: JobProgressReporter
) : QuestionGenerator {

    override suspend fun generate(
        spec: QuestionGenerationSpec,
        files: List<ExtractableFile>
    ): List<QuestionDTO> = coroutineScope {
        val job = jobService.findById(spec.jobId)
            ?: throwJobNotFoundException(spec.jobId)

        reporter.update(job, Progress.QUESTIONS_GENERATION_STARTED)

        val rawContent = contentPreparer.prepare(files)
        ensureActive()

//        val optimizedContent = optimizer.optimize(job, rawContent)
//        ensureActive()

        allocationGenerator.generate(job, rawContent, spec.allocations)
    }
}