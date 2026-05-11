package com.xapps.question_generator.workflow.questions_json_parser.question_parser.fib

import com.xapps.platform.core.string.generateUniqueId
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.ExplanationPolicy
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.JsonSchemaValidator
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.SelfTestQuestionTypeParser
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.arrayStrings
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.string
import com.xapps.model.Difficulty
import com.xapps.model.QuestionType
import com.xapps.questions.contracts.self_test_generation.dto.QuestionDTO
import org.springframework.stereotype.Service
import kotlinx.serialization.json.*

@Service
class FibQuestionParser(
    private val schemaValidator: JsonSchemaValidator,
    private val explanationPolicy: ExplanationPolicy
) : SelfTestQuestionTypeParser() {

    override val supportedType = QuestionType.FIB

    override fun parse(
        obj: JsonObject,
        difficulty: Difficulty
    ): QuestionDTO {
        schemaValidator.requireFields(
            obj,
            TEXT,
            TOPIC,
            ACCEPTABLE_ANSWERS
        )

        val questionId = generateUniqueId()
        val text = obj.string(TEXT)
        val topic = obj.string(TOPIC)
        val explanation = explanationPolicy.extract(obj, difficulty)

        val answers = obj.arrayStrings(ACCEPTABLE_ANSWERS).toSet()
        require(answers.isNotEmpty()) { "Q: acceptableAnswers empty" }

        return QuestionDTO(
            questionType = QuestionType.FIB,
            id = questionId,
            text = text,
            explanation = explanation,
            topic = topic,
            difficulty = difficulty,
            acceptableAnswers = answers,

            options = emptyList(),
            correctOptionId = null,
            correctOptionIds = emptySet()
        )
    }
}
