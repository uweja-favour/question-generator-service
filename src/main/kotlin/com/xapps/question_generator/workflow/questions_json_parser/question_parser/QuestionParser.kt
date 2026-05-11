package com.xapps.question_generator.workflow.questions_json_parser.question_parser

import com.xapps.model.Difficulty
import com.xapps.model.QuestionType
import com.xapps.questions.contracts.self_test_generation.dto.QuestionDTO
import kotlinx.serialization.json.JsonObject
import org.springframework.stereotype.Service

@Service
class QuestionParser(
    parsers: List<SelfTestQuestionTypeParser>
) {

    private val parserMap = parsers.associateBy { it.supportedType }

    fun parse(
        type: QuestionType,
        obj: JsonObject,
        difficulty: Difficulty
    ): QuestionDTO {
        val parser = parserMap[type]
            ?: error("No parser registered for question type $type")

        return parser.parse(obj, difficulty)
    }
}
