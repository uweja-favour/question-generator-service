package com.xapps.question_generator.workflow.pipeline

import com.xapps.question_generator.infrastructure.object_store.ExtractableFile
import com.xapps.questions.contracts.self_test_generation.dto.QuestionDTO
import com.xapps.questions.contracts.self_test_generation.model.QuestionGenerationSpec
import org.springframework.web.multipart.MultipartFile

interface QuestionGenerator {
    suspend fun generate(
        spec: QuestionGenerationSpec,
        files: List<ExtractableFile>
    ): List<QuestionDTO>
}
