package com.xapps.question_generator.workflow.questions_json_parser.question_parser.ms

import com.xapps.platform.core.string.generateUniqueId
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.ExplanationPolicy
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.JsonSchemaValidator
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.SelfTestOptionParser
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.SelfTestQuestionTypeParser
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.arrayStrings
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.string
import com.xapps.model.Difficulty
import com.xapps.model.QuestionType
import com.xapps.questions.contracts.self_test_generation.dto.QuestionDTO
import org.springframework.stereotype.Service
import kotlinx.serialization.json.*

@Service
class MsQuestionParser(
    private val schemaValidator: JsonSchemaValidator,
    private val optionParser: SelfTestOptionParser,
    private val explanationPolicy: ExplanationPolicy
) : SelfTestQuestionTypeParser() {

    override val supportedType = QuestionType.MS

    override fun parse(
        obj: JsonObject,
        difficulty: Difficulty
    ): QuestionDTO {
        schemaValidator.requireFields(
            obj = obj,
            TEXT,
            OPTIONS,
            TOPIC,
            CORRECT_OPTIONS
        )

        val questionId = generateUniqueId()
        val text = obj.string(TEXT)
        val topic = obj.string(TOPIC)

        val correctOptionTexts = obj.arrayStrings(CORRECT_OPTIONS).toSet()
        require(correctOptionTexts.isNotEmpty()) { "Q: no correct labels" }

        val explanation = explanationPolicy.extract(obj, difficulty)

        val options = optionParser.parse(obj, questionId)
        require(options.size >= 2) { "Q#$this: MS requires ≥2 options" }


        val correctOptionIds = options
            .filter { it.text in correctOptionTexts }
            .map { it.id }

        require(correctOptionIds.isNotEmpty()) { "Q#$this: correctOptionsIds empty" }

        return QuestionDTO(
            questionType = QuestionType.MS,
            id = questionId,
            text = text,
            explanation = explanation,
            topic = topic,
            difficulty = difficulty,
            options = options,
            correctOptionIds = correctOptionIds.toSet(),

            correctOptionId = null,
            acceptableAnswers = emptySet(),
        )
    }
}
