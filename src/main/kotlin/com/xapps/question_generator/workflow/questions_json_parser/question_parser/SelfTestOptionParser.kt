package com.xapps.question_generator.workflow.questions_json_parser.question_parser

import com.xapps.platform.core.string.generateUniqueId
import com.xapps.question_generator.workflow.QuestionSchemaHolder
import com.xapps.model.QuestionId
import com.xapps.questions.contracts.self_test_generation.dto.OptionDTO
import kotlinx.serialization.json.*
import org.springframework.stereotype.Component

@Component
class SelfTestOptionParser : QuestionSchemaHolder() {

    fun parse(
        obj: JsonObject,
        questionId: QuestionId,
    ): List<OptionDTO> {

        val optionsArray = obj[OPTIONS] as? JsonArray
            ?: error("Q#$obj: '$OPTIONS' must exist and be an array")

        if (optionsArray.isEmpty()) {
            error("Q#$obj: '$OPTIONS' must not be empty")
        }

        return optionsArray.mapIndexed { index, element ->
            element.toOption(
                questionId = questionId,
                optionIndex = index,
            )
        }
    }

    private fun JsonElement.toOption(
        questionId: QuestionId,
        optionIndex: Int,
    ): OptionDTO {

        return OptionDTO(
            id = generateUniqueId(),
            text = jsonPrimitiveOrError(),
            label = (optionIndex + 1).toString(),
            questionId = questionId
        )
    }

    private fun JsonElement.jsonPrimitiveOrError(): String {
        val primitive = this as? JsonPrimitive
            ?: error("Q#$this: option must be a string")

        if (!primitive.isString) {
            error("Q#$this: option must be a string")
        }

        return primitive.content
    }
}
