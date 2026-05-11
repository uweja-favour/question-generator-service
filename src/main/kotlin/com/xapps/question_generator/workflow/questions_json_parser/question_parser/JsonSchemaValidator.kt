package com.xapps.question_generator.workflow.questions_json_parser.question_parser

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.springframework.stereotype.Component

@Component
class JsonSchemaValidator {

    fun requireFields(
        obj: JsonObject,
        vararg fields: String
    ) {
        fields.forEach { field ->
            val el = obj[field]
                ?: error("Q#$obj missing field '$field'")

            when (el) {
                is JsonPrimitive ->
                    require(!el.contentOrNull.isNullOrBlank()) {
                        "Q#$obj: '$field' must be non-blank"
                    }
                is JsonArray ->
                    require(el.isNotEmpty()) {
                        "Q#$obj: '$field' must be non-empty array"
                    }
                else -> error("Un-recognized JsonElement type: $el")
            }
        }
    }
}
