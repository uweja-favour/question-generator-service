package com.xapps.question_generator.new_prompt_builder

import com.xapps.model.Difficulty
import com.xapps.model.QuestionType
import com.xapps.question_generator.workflow.QuestionSchemaHolder
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.arrayStrings
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.arrayStringsOrNull
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.string
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.stringOrNull
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Validates a parsed question map against the structural constraints defined
 * in [QuestionTypeConfigRegistry].
 *
 * Returns a [ValidationResult] that callers can inspect — clean separation
 * between validation logic and retry/error-handling logic.
 */
@Component
class QuestionValidator(
    private val constraintComputer: ConstraintComputer
) : QuestionSchemaHolder() {

    private val log = LoggerFactory.getLogger(javaClass)

    fun validate(
        question: JsonObject,
        questionType: QuestionType,
        difficulty: Difficulty
    ): ValidationResult {
        val failures = mutableListOf<String>()

        // Common field presence checks
        failures += checkRequiredFields(question, questionType)

        // Difficulty-specific explanation rule
        failures += checkExplanationPolicy(question, difficulty)

        // Type-specific constraint checks
        val constraints = constraintComputer.compute(questionType)
        failures += when (questionType) {
            QuestionType.MC  -> validateMc(question, constraints as ComputedConstraints.ForMC)
            QuestionType.MS  -> validateMs(question, constraints as ComputedConstraints.ForMS)
            QuestionType.TF  -> validateTf(question)
            QuestionType.FIB -> validateFib(question, constraints as ComputedConstraints.ForFIB)
        }

        return if (failures.isEmpty()) ValidationResult.Valid
        else ValidationResult.Invalid(failures)
    }

    private fun checkRequiredFields(
        question: JsonObject,
        questionType: QuestionType
    ): List<String> {
        val required = baseRequiredFields(questionType)
        return required
            .filter { field -> !question.containsKey(field) || question[field] == null }
            .map { "Missing required field: '$it'" }
    }

    private fun baseRequiredFields(questionType: QuestionType): List<String> =
        when (questionType) {
            QuestionType.MC  -> listOf(TEXT, TOPIC, DIFFICULTY, QUESTION_TYPE, OPTIONS, CORRECT_OPTION_TEXT)
            QuestionType.MS  -> listOf(TEXT, TOPIC, DIFFICULTY, QUESTION_TYPE, OPTIONS, CORRECT_OPTIONS)
            QuestionType.TF  -> listOf(TEXT, TOPIC, DIFFICULTY, QUESTION_TYPE, OPTIONS, CORRECT_OPTION_TEXT)
            QuestionType.FIB -> listOf(TEXT, TOPIC, DIFFICULTY, QUESTION_TYPE, ACCEPTABLE_ANSWERS)
        }

    private fun checkExplanationPolicy(
        question: JsonObject,
        difficulty: Difficulty
    ): List<String> {
        val hasExplanation = question.containsKey(EXPLANATION) &&
                (question.stringOrNull(EXPLANATION))?.isNotBlank() == true

        return when (difficulty) {
            Difficulty.VERY_EASY, Difficulty.EASY -> {
                if (hasExplanation)
                    listOf("Field '$EXPLANATION' must not be present for difficulty ${difficulty.name}")
                else emptyList()
            }
            Difficulty.MEDIUM, Difficulty.HARD, Difficulty.VERY_HARD -> {
                if (!hasExplanation)
                    listOf("Field '$EXPLANATION' must be present and non-blank for difficulty ${difficulty.name}")
                else emptyList()
            }
        }
    }

    private fun validateMc(
        question: JsonObject,
        c: ComputedConstraints.ForMC
    ): List<String> {
        val failures = mutableListOf<String>()
        val options = question.optionsList() ?: run {
            failures += "'$OPTIONS' must be a list of strings"
            return failures
        }
        if (options.size != c.totalOptions)
            failures += "'$OPTIONS' must have exactly ${c.totalOptions} entries, found ${options.size}"

        val correctOption = question.stringOrNull(CORRECT_OPTION_TEXT)
        if (correctOption == null)
            failures += "'$CORRECT_OPTION_TEXT' must be a non-null string"
        else if (correctOption !in options)
            failures += "'$CORRECT_OPTION_TEXT' value \"$correctOption\" is not present in '$OPTIONS'"

        return failures
    }

    private fun validateMs(
        question: JsonObject,
        c: ComputedConstraints.ForMS
    ): List<String> {
        log.info("first")
        val failures = mutableListOf<String>()
        val options = question.optionsList() ?: run {
            failures += "'$OPTIONS' must be a list of strings"
            return failures
        }
        log.info("second")
        if (options.size != c.totalOptions)
            failures += "'$OPTIONS' must have exactly ${c.totalOptions} entries, found ${options.size}"
        log.info("third")
        @Suppress("UNCHECKED_CAST")
        val correctOptions = question.arrayStringsOrNull(CORRECT_OPTIONS) ?: run {
            failures += "'$CORRECT_OPTIONS' must be a list of strings"
            return failures
        }

        log.info("fourth")

        if (correctOptions.size < c.minCorrectOptions)
            failures += "'$CORRECT_OPTIONS' must have at least ${c.minCorrectOptions} values, found ${correctOptions.size}"

        if (correctOptions.size > c.maxCorrectOptions)
            failures += "'$CORRECT_OPTIONS' must have at most ${c.maxCorrectOptions} values, found ${correctOptions.size}"

        val orphans = correctOptions.filter { it !in options }
        if (orphans.isNotEmpty())
            failures += "Values in '$CORRECT_OPTIONS' not found verbatim in '$OPTIONS': $orphans"

        val distractors = options.size - correctOptions.size
        if (distractors < c.minDistractors)
            failures += "At least ${c.minDistractors} distractor(s) required; found $distractors"

        log.info("finally")
        return failures
    }

    private fun validateTf(question: JsonObject): List<String> {
        val failures = mutableListOf<String>()
        val options = question.optionsList()?.map { it.lowercase() }
        log.info("Options in validateTf are: $options"
        )
        if (options != listOf("true", "false") && options != listOf("false", "true"))
            failures += "'$OPTIONS' must be exactly [\"True\", \"False\"] or [\"False\", \"True\"]. Found: $options"

        val correct = question.stringOrNull(CORRECT_OPTION_TEXT)
        if (correct?.lowercase() != "true" && correct?.lowercase() != "false")
            failures += "'$CORRECT_OPTION_TEXT' must be \"True\" or \"False\", found: $correct"

        return failures
    }

    private fun validateFib(
        question: JsonObject,
        c: ComputedConstraints.ForFIB
    ): List<String> {
        val failures = mutableListOf<String>()
        val text = question.stringOrNull(TEXT)
            ?: return listOf("'$TEXT' must be a non-null string")

        val blankCount = text.split("___").size - 1
        if (blankCount != 1)
            failures += "'$TEXT' must contain exactly 1 ___ placeholder, found $blankCount"

        @Suppress("UNCHECKED_CAST")
        val answers = question.arrayStringsOrNull(ACCEPTABLE_ANSWERS) ?: run {
            failures += "'$ACCEPTABLE_ANSWERS' must be a list of strings"
            return failures
        }

        if (answers.size < c.minAcceptableAnswers)
            failures += "'$ACCEPTABLE_ANSWERS' must have at least ${c.minAcceptableAnswers} values, found ${answers.size}"

        if (answers.size > c.maxAcceptableAnswers)
            failures += "'$ACCEPTABLE_ANSWERS' must have at most ${c.maxAcceptableAnswers} values, found ${answers.size}"

        return failures
    }

    @Suppress("UNCHECKED_CAST")
    private fun JsonObject.optionsList(): List<String>? =
        runCatching {
            (this[OPTIONS] as? JsonArray)
                ?.map { jsonElement ->
                    require(jsonElement is JsonPrimitive) {
                        "Option Json Element is not a JsonPrimitive: $jsonElement"
                    }

                    jsonElement.content
                }
        }.getOrElse {
            log.error("An error occurred in optionsList: $it")
            null
        }
}

sealed interface ValidationResult {
    object Valid : ValidationResult
    data class Invalid(val failures: List<String>) : ValidationResult {
        fun summary(): String =
            """
                Some failures occurred.
                This is the summary of the failures.
                
                ${failures.joinToString("; ")}
                END OF FAILURE SUMMARY.
            """.trimIndent()
    }
}