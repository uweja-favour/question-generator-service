package com.xapps.question_generator.workflow.questions_json_parser.question_parser

import kotlinx.serialization.json.*

fun JsonObject.string(key: String): String =
    this[key]?.jsonPrimitive?.contentOrNull
        ?: error("Q#$this missing '$key'")

fun JsonObject.stringOrNull(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull

fun JsonObject.optionalString(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

fun JsonObject.arrayStrings(key: String): List<String> =
    (this[key] as? JsonArray)
        ?.mapNotNull { it.jsonPrimitive.contentOrNull }
        ?: error("Q#$this missing array '$key'")

fun JsonObject.arrayStringsOrNull(key: String): List<String>? =
    (this[key] as? JsonArray)
        ?.mapNotNull { it.jsonPrimitive.contentOrNull }
