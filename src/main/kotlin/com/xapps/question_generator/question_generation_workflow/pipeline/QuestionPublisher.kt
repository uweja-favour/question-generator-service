package com.xapps.question_generator.question_generation_workflow.pipeline

import com.xapps.question_generation.dto.QuestionDTO
import com.xapps.question_generation.QuestionGenerationSpec

interface QuestionPublisher {
    suspend fun publishQuestions(
        questions: List<QuestionDTO>,
        spec: QuestionGenerationSpec,
    )
}