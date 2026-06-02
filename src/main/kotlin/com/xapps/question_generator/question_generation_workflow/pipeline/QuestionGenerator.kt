package com.xapps.question_generator.question_generation_workflow.pipeline

import com.xapps.question_generator.infrastructure.object_store.ExtractableFile
import com.xapps.question_generation.dto.QuestionDTO
import com.xapps.question_generation.QuestionGenerationSpec

interface QuestionGenerator {
    suspend fun generate(
        spec: QuestionGenerationSpec,
        files: List<ExtractableFile>
    ): List<QuestionDTO>
}
