package com.xapps.question_generator.workflow.questions_json_parser

import com.xapps.question_generator.workflow.QuestionSchemaHolder
import com.xapps.model.Difficulty
import com.xapps.model.QuestionType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object JsonQuestionInspector : QuestionSchemaHolder() {

    fun detectDifficulty(obj: JsonObject): Difficulty? =
        runCatching {
            obj[DIFFICULTY]
                ?.jsonPrimitive
                ?.content
                ?.trim()
                ?.uppercase()
                ?.let(Difficulty::valueOf)
        }.getOrNull()

    fun detectType(obj: JsonObject): QuestionType? =
        runCatching {
            obj[QUESTION_TYPE]
                ?.jsonPrimitive
                ?.content
                ?.trim()
                ?.uppercase()
                ?.let(QuestionType::valueOf)
        }.getOrNull()
}