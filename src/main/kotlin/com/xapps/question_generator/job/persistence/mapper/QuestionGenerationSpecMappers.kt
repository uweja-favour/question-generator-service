package com.xapps.question_generator.job.persistence.mapper

import com.xapps.model.QuizType
import com.xapps.question_generator.job.persistence.entity.QuestionGenerationSpecDocument
import com.xapps.questions.contracts.question_generation.JobId
import com.xapps.questions.contracts.self_test_generation.model.QuestionGenerationSpec

fun QuestionGenerationSpec.toEntity(): QuestionGenerationSpecDocument {
    return QuestionGenerationSpecDocument(
        id = id,
        allocations = allocations.map { it.toEntity() },
        jobId = jobId.value,
        userId = userId,
        quizId = quizId,
        quizTypeCode = quizType.code,
        questionCount = questionCount,
        fileKeys = fileKeys
    )
}

fun QuestionGenerationSpecDocument.toDomain(): QuestionGenerationSpec {

    val quizType = requireNotNull(QuizType.fromCodeOrNull(quizTypeCode)) {
        "Unknown quiz type code: $quizTypeCode"
    }

    return QuestionGenerationSpec(
        id = id,
        quizType = quizType,
        userId = userId,
        quizId = quizId,
        questionCount = questionCount,
        allocations = allocations.map { it.toDomain() },
        fileKeys = fileKeys,
        jobId = JobId.of(jobId)
    )
}