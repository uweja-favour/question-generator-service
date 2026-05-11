package com.xapps.question_generator.workflow.questions_json_parser.question_parser.tf

import com.xapps.platform.core.string.generateUniqueId
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.ExplanationPolicy
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.JsonSchemaValidator
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.SelfTestOptionParser
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.SelfTestQuestionTypeParser
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.string
import com.xapps.model.Difficulty
import com.xapps.model.QuestionType
import com.xapps.questions.contracts.self_test_generation.dto.QuestionDTO
import org.springframework.stereotype.Service
import kotlinx.serialization.json.*

@Service
class TfQuestionParser(
    private val schemaValidator: JsonSchemaValidator,
    private val optionParser: SelfTestOptionParser,
    private val explanationPolicy: ExplanationPolicy
) : SelfTestQuestionTypeParser() {

    override val supportedType = QuestionType.TF

    override fun parse(
        obj: JsonObject,
        difficulty: Difficulty
    ): QuestionDTO {
        schemaValidator.requireFields(
            obj,
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
            .distinctBy { it.text.lowercase() }

        val boolValues = options.mapNotNull {
            it.text.lowercase().toBooleanStrictOrNull()
        }
        require(boolValues.contains(true) && boolValues.contains(false)) {
            "Q#$obj: TF must contain both true and false. It contained: $boolValues"
        }

        val correctOptionId = options.first { it.text == correctOptionText }.id

        return QuestionDTO(
            questionType = QuestionType.TF,
            id = questionId,
            text = text,
            explanation = explanation,
            topic = topic,
            difficulty = difficulty,
            options = options,
            correctOptionId = correctOptionId,


            correctOptionIds = emptySet(),
            acceptableAnswers = emptySet()
        )
    }
}
