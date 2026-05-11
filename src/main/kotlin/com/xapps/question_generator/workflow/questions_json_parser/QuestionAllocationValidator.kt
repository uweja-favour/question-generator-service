package com.xapps.question_generator.workflow.questions_json_parser

import com.xapps.questions.contracts.question_generation.QuestionAllocation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.springframework.stereotype.Service

@Service
class QuestionAllocationValidator {

    suspend fun validate(
        questions: JsonArray,
        allocation: QuestionAllocation
    ) = coroutineScope {
        require(questions.size == allocation.count) {
            "Expected ${allocation.count} questions, found ${questions.size}."
        }

        questions.mapIndexed { index, element ->
            async {
                validateSingle(element.jsonObject, allocation, index)
            }
        }.awaitAll()
    }

    private fun validateSingle(
        obj: JsonObject,
        allocation: QuestionAllocation,
        index: Int
    ) {
        val type = JsonQuestionInspector.detectType(obj)
        val difficulty = JsonQuestionInspector.detectDifficulty(obj)

        require(type == allocation.questionType) {
            "Question #${index + 1} type mismatch. Expected ${allocation.questionType}, found $type."
        }

        require(difficulty == allocation.difficulty) {
            "Question #${index + 1} difficulty mismatch. Expected ${allocation.difficulty}, found $difficulty."
        }
    }
}