package com.xapps.question_generator.workflow.questions_json_parser.question_parser.mc

import com.xapps.platform.core.string.generateUniqueId
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.ExplanationPolicy
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.JsonSchemaValidator
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.SelfTestOptionParser
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.SelfTestQuestionTypeParser
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.string
import com.xapps.model.Difficulty
import com.xapps.model.QuestionType
import com.xapps.questions.contracts.self_test_generation.dto.QuestionDTO
import kotlinx.serialization.json.JsonObject
import org.springframework.stereotype.Service

@Service
class McQuestionParser(
    private val schemaValidator: JsonSchemaValidator,
    private val optionParser: SelfTestOptionParser,
    private val explanationPolicy: ExplanationPolicy
) : SelfTestQuestionTypeParser() {

    override val supportedType = QuestionType.MC

    override fun parse(
        obj: JsonObject,
        difficulty: Difficulty
    ): QuestionDTO {
        schemaValidator.requireFields(
            obj = obj,
            TEXT,
            OPTIONS,
            TOPIC,
            CORRECT_OPTION_TEXT
        )

        val questionId = generateUniqueId()
        val text = obj.string(TEXT)
        val topic = obj.string(TOPIC)
        val correctOptionText = obj.string(CORRECT_OPTION_TEXT)
        val explanation = explanationPolicy.extract(obj, difficulty)

        val options = optionParser.parse(obj, questionId)

        require(options.size >= 2) { "Q: MC requires ≥2 options" }

        val correctOptionId = options.single { it.text == correctOptionText }.id

        return QuestionDTO(
            questionType = supportedType,
            id = questionId,
            text = text,
            explanation = explanation,
            topic = topic,
            difficulty = difficulty,
            options = options,
            correctOptionId = correctOptionId,

            correctOptionIds = emptySet(),
            acceptableAnswers = emptySet(),
        )
    }
}
