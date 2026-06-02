package com.xapps.question_generation.dto

import com.xapps.dto.QuestionAllocationDTO
import com.xapps.question_generation.QuestionAllocation

fun QuestionAllocationDTO.toDomain(
    id: String,
    specId: String
): QuestionAllocation =
    QuestionAllocation(
        id = id,
        specId = specId,
        questionType = type,
        difficulty = difficulty,
        count = count
    )