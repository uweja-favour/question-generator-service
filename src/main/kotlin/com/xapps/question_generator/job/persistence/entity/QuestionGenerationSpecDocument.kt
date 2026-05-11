package com.xapps.question_generator.job.persistence.entity

import com.xapps.model.QuizId
import com.xapps.model.QuizTypeCode
import com.xapps.question_generator.infrastructure.object_store.ObjectKey

data class QuestionGenerationSpecDocument(
    val id: String,

    val jobId: String,

    val userId: String,

    val quizId: QuizId,

    val allocations: List<QuestionAllocationDocument>,

    val quizTypeCode: QuizTypeCode,

    val questionCount: Int,

    val fileKeys: List<ObjectKey>
)