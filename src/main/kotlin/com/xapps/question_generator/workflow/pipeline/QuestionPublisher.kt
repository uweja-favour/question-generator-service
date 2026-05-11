package com.xapps.question_generator.workflow.pipeline

import com.xapps.questions.contracts.self_test_generation.dto.QuestionDTO
import com.xapps.questions.contracts.self_test_generation.model.QuestionGenerationSpec

interface QuestionPublisher {
    suspend fun publishQuestions(
        questions: List<QuestionDTO>,
        spec: QuestionGenerationSpec,
    )
}