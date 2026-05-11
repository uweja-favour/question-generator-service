package com.xapps.question_generator.job.persistence.entity

import com.xapps.model.Difficulty
import com.xapps.model.QuestionTypeCode

data class QuestionAllocationDocument(
    val id: String,

    val specId: String,

    val count: Int,

    val questionTypeCode: QuestionTypeCode,

    val difficulty: Difficulty
)