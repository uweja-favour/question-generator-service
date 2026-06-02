package com.xapps.question_generation

import com.xapps.dto.QuestionAllocationDTO
import com.xapps.model.QuizId
import com.xapps.model.QuizType
import com.xapps.question_generation.dto.toDomain
import kotlinx.serialization.Serializable

@Serializable
data class QuestionGenerationSpec(
    val id: String,
    val userId: String,
    val quizId: QuizId,
    val questionCount: Int,
    val allocations: List<QuestionAllocation>,
    val fileKeys: List<String>,
    val jobId: JobId,
    val quizType: QuizType
)

fun createQuestionGenerationSpec(
    userId: String,
    quizId: QuizId,
    jobId: JobId,
    questionCount: Int,
    fileKeys: List<String>,
    allocations: List<QuestionAllocationDTO>,
    quizType: QuizType,
    idGenerator: () -> String,
): QuestionGenerationSpec {
    val specId = idGenerator()
    return QuestionGenerationSpec(
        id = specId,
        userId = userId,
        quizId = quizId,
        questionCount = questionCount,
        allocations = allocations.map {
            it.toDomain(
                id = idGenerator(),
                specId = specId
            )
        },
        fileKeys = fileKeys,
        jobId = jobId,
        quizType = quizType
    )
}