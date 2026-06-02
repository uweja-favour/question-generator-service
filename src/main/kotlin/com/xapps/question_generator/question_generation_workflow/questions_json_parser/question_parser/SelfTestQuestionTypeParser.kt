package com.xapps.question_generator.question_generation_workflow.questions_json_parser.question_parser

import com.xapps.question_generator.question_generation_workflow.QuestionSchemaHolder
import com.xapps.model.Difficulty
import com.xapps.model.QuestionType
import com.xapps.question_generation.dto.QuestionDTO
import kotlinx.serialization.json.JsonObject

abstract class SelfTestQuestionTypeParser : QuestionSchemaHolder() {
    abstract val supportedType: QuestionType

    abstract fun parse(
        obj: JsonObject,
        difficulty: Difficulty
    ): QuestionDTO
}
