//package com.xapps.question_generator.workflow.self_test.questions_json_parser.question_parser
//
//import com.xapps.platform.core.string.generateUniqueId
//import com.xapps.question_generator.workflow.self_test.SelfTestSchemaHolder
//import com.xapps.questions.contracts.self_test_generation.dto.SelfTestOptionDto
//import com.xapps.questions.contracts.self_test_generation.dto.SelfTestQuestionDto
//import com.xapps.questions.contracts.model.Difficulty
//import com.xapps.questions.contracts.model.QuestionId
//import com.xapps.questions.contracts.model.QuestionType
//import kotlinx.serialization.json.*
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Service
//
///**
// * Parser for Self-Test Questions JSON → DTOs using kotlinx.serialization.json
// * Fully idiomatic Kotlin, functional validation, and explicit.
// */
//@Service
//class SelfTestQuestionParser : SelfTestSchemaHolder() {
//
//    private val logger = LoggerFactory.getLogger(javaClass)
//
//    fun parse(type: QuestionType, obj: JsonObject, idx: Int, diff: Difficulty): SelfTestQuestionDto {
//        return when (type) {
//            QuestionType.MC -> parseSingleMC(obj, idx + 1, diff)
//            QuestionType.MS -> parseSingleMS(obj, idx + 1, diff)
//            QuestionType.TF -> parseSingleTF(obj, idx + 1, diff)
//            QuestionType.FIB -> parseSingleFIB(obj, idx + 1, diff)
//        }
//    }
//
//    private fun parseSingleMC(obj: JsonObject, number: Int, diff: Difficulty): SelfTestQuestionDto =
//        obj.ensure(number) {
//            string(TEXT)
//            array(OPTIONS)
//            string(TOPIC)
//            string(CORRECT_OPTION_ID)
//        }.let { accessor ->
//            val questionId = generateUniqueId()
//            val text = accessor.string(TEXT)
//            val topic = accessor.string(TOPIC)
//            val correctLabel = accessor.string(CORRECT_OPTION_ID)
//            val explanation = accessor.optional(EXPLANATION).also { validateExplanation(it, diff, number) }.orEmpty()
//            val options = accessor.options(OPTIONS, questionId).also {
//                require(it.size >= 2) { "Q#$number: MC requires ≥2 options" }
//                require(it.any { o -> o.label == correctLabel }) { "Q#$number: correct label '$correctLabel' not found" }
//            }
//            val correctOptionId = options.find { it.label == correctLabel }?.id
//                ?: error("Q#$number: correctOptionId not found")
//
//            SelfTestQuestionDto(
//                questionType = QuestionType.MC,
//                id = questionId,
//                number = number,
//                text = text,
//                explanation = explanation,
//                topic = topic,
//                difficulty = diff,
//                options = options,
//                correctOptionId = correctOptionId
//            )
//        }
//
//    private fun parseSingleMS(obj: JsonObject, number: Int, diff: Difficulty): SelfTestQuestionDto =
//        obj.ensure(number) {
//            string(TEXT)
//            array(OPTIONS)
//            string(TOPIC)
//            array(CORRECT_OPTIONS_IDS)
//        }.let { accessor ->
//            val questionId = generateUniqueId()
//            val text = accessor.string(TEXT)
//            val topic = accessor.string(TOPIC)
//            val explanation = accessor.optional(EXPLANATION).also { validateExplanation(it, diff, number) }.orEmpty()
//            val correctLabels = accessor.array(CORRECT_OPTIONS_IDS).mapNotNull { it.jsonPrimitive.contentOrNull }.distinct()
//            require(correctLabels.isNotEmpty()) { "Q#$number: no correct labels" }
//
//            val options = accessor.options(OPTIONS, questionId).also {
//                require(it.size >= 2) { "Q#$number: MS requires ≥2 options" }
//                require(correctLabels.all { l -> it.any { o -> o.label == l } }) { "Q#$number: some correct labels missing" }
//            }
//            val correctOptionIds = options.filter { it.label in correctLabels }.map { it.id }
//            require(correctOptionIds.isNotEmpty()) { "Q#$number: correctOptionsIds empty" }
//
//            SelfTestQuestionDto(
//                questionType = QuestionType.MS,
//                id = questionId,
//                number = number,
//                text = text,
//                explanation = explanation,
//                topic = topic,
//                difficulty = diff,
//                options = options,
//                correctOptionsIds = correctOptionIds
//            )
//        }
//
//    private fun parseSingleTF(obj: JsonObject, number: Int, diff: Difficulty): SelfTestQuestionDto =
//        obj.ensure(number) {
//            string(TEXT)
//            array(OPTIONS)
//            string(TOPIC)
//            string(CORRECT_OPTION_ID)
//        }.let { accessor ->
//            val questionId = generateUniqueId()
//            val text = accessor.string(TEXT)
//            val topic = accessor.string(TOPIC)
//            val correctLabel = accessor.string(CORRECT_OPTION_ID)
//            val explanation = accessor.optional(EXPLANATION).also { validateExplanation(it, diff, number) }.orEmpty()
//
//            val options = accessor.options(OPTIONS, questionId).also {
//                require(it.size >= 2) { "Q#$number: TF must have ≥2 options" }
//                val bools = it.mapNotNull { o -> o.text.lowercase().toBooleanStrictOrNull() }
//                require(bools.size >= 2) { "Q#$number: TF must include both True and False" }
//                require(it.any { o -> o.label == correctLabel }) { "Q#$number: correct label '$correctLabel' missing" }
//            }
//            val correctOptionId = options.find { it.label == correctLabel }?.id
//                ?: error("Q#$number: correctOptionId not found")
//
//            SelfTestQuestionDto(
//                questionType = QuestionType.TF,
//                id = questionId,
//                number = number,
//                text = text,
//                explanation = explanation,
//                topic = topic,
//                difficulty = diff,
//                options = options,
//                correctOptionId = correctOptionId
//            )
//        }
//
//    private fun parseSingleFIB(obj: JsonObject, number: Int, diff: Difficulty): SelfTestQuestionDto =
//        obj.ensure(number) {
//            string(TEXT)
//            string(TOPIC)
//            array(ACCEPTABLE_ANSWERS)
//        }.let { accessor ->
//            val questionId = generateUniqueId()
//            val text = accessor.string(TEXT)
//            val topic = accessor.string(TOPIC)
//            val explanation = accessor.optional(EXPLANATION).also { validateExplanation(it, diff, number) }.orEmpty()
//            val answers = accessor.array(ACCEPTABLE_ANSWERS)
//                .mapNotNull { it.jsonPrimitive.contentOrNull }
//                .distinct()
//                .toSet()
//            require(answers.isNotEmpty()) { "Q#$number: acceptableAnswers empty" }
//
//            SelfTestQuestionDto(
//                questionType = QuestionType.FIB,
//                id = questionId,
//                number = number,
//                text = text,
//                explanation = explanation,
//                topic = topic,
//                difficulty = diff,
//                acceptableAnswers = answers
//            )
//        }
//
//
//    // --- Functional DSL for validation and field access -------------------
//    private inline fun JsonObject.ensure(index: Int, schema: FieldSchema.() -> Unit): JsonAccessor {
//        val fs = FieldSchema(this, index)
//        fs.schema()
//        fs.validate()
//        return JsonAccessor(this)
//    }
//
//    private class FieldSchema(private val obj: JsonObject, private val number: Int) {
//        private val required = mutableListOf<Pair<String, JsonFieldType>>()
//        fun string(name: String) = required.add(name to JsonFieldType.STRING)
//        fun array(name: String) = required.add(name to JsonFieldType.ARRAY)
//        fun validate() = required.forEach { (field, type) ->
//            val el = obj[field] ?: error("Q#$number missing field '$field'")
//            when (type) {
//                JsonFieldType.STRING -> if (el !is JsonPrimitive || el.contentOrNull.isNullOrBlank())
//                    error("Q#$number: '$field' must be non-blank string")
//                JsonFieldType.ARRAY -> if (el !is JsonArray || el.isEmpty())
//                    error("Q#$number: '$field' must be non-empty array")
//                JsonFieldType.OBJECT -> if (el !is JsonObject)
//                    error("Q#$number: '$field' must be object")
//            }
//        }
//    }
//
//    private class JsonAccessor(private val obj: JsonObject) : SelfTestSchemaHolder() {
//        fun string(key: String): String =
//            obj[key]?.jsonPrimitive?.contentOrNull ?: error("Missing '$key'")
//
//        fun optional(key: String): String? =
//            obj[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
//
//        fun array(key: String): List<JsonElement> =
//            (obj[key] as? JsonArray)?.toList() ?: emptyList()
//
//        fun options(field: String, questionId: QuestionId): List<SelfTestOptionDto> =
//            array(field).distinct().map { it.jsonObject.option(questionId) }
//
//        private fun JsonObject.option(questionId: QuestionId): SelfTestOptionDto {
//            val accessor = JsonAccessor(this)
//            val label = accessor.string(LABEL)
//            val text = accessor.string(TEXT)
//            return SelfTestOptionDto(
//                id = generateUniqueId(),
//                text = text,
//                label = label,
//                questionId = questionId
//            )
//        }
//    }
//
//    private fun validateExplanation(expl: String?, diff: Difficulty, idx: Int) {
//        val has = !expl.isNullOrBlank()
//        when (diff) {
//            Difficulty.VERY_EASY, Difficulty.EASY -> if (has)
//                logger.info("Q#$idx: explanation MUST NOT be present for ${diff.displayName}")
//            Difficulty.MEDIUM, Difficulty.HARD, Difficulty.VERY_HARD -> if (!has)
//                logger.info("Q#$idx: explanation required for difficulty $diff")
//        }
//    }
//}
//
//private enum class JsonFieldType {
//    STRING,
//    ARRAY,
//    OBJECT
//}
