package com.xapps.question_generator.job.persistence.mapper

import com.xapps.model.QuestionType
import com.xapps.question_generator.job.persistence.entity.QuestionAllocationDocument
import com.xapps.questions.contracts.question_generation.QuestionAllocation

fun QuestionAllocation.toEntity(): QuestionAllocationDocument {
    return QuestionAllocationDocument(
        id = id,
        specId = specId,
        count = count,
        questionTypeCode = questionType.code,
        difficulty = difficulty
    )
}

fun QuestionAllocationDocument.toDomain(): QuestionAllocation {

    val questionType = requireNotNull(QuestionType.fromCodeOrNull(questionTypeCode)) {
        "Unknown question type code: $questionTypeCode"
    }

    return QuestionAllocation(
        id = id,
        specId = specId,
        count = count,
        questionType = questionType,
        difficulty = difficulty
    )
}