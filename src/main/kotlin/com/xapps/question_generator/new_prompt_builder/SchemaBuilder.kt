package com.xapps.question_generator.new_prompt_builder

import com.xapps.model.Difficulty
import com.xapps.model.QuestionType
import com.xapps.questions.contracts.question_generation.QuestionAllocation
import com.xapps.question_generator.workflow.QuestionSchemaHolder
import org.springframework.stereotype.Component
/**
 * Builds the SCHEMA CONTRACT section of the generation prompt.
 *
 * Numeric constraints (option counts, correct option counts, acceptable answer
 * counts) are injected as concrete numbers — never described in prose the LLM
 * might misinterpret or ignore.
 *
 * The [EXPLANATION] field appears in both the schema block and the HARD RULES
 * only when the difficulty warrants it. When it does not, the schema block omits
 * the field entirely and the rules explicitly forbid its presence — so the model
 * receives one consistent, unambiguous instruction either way.
 */
@Component
class SchemaBuilder(
    private val constraintComputer: ConstraintComputer
) : QuestionSchemaHolder() {

    fun buildSchema(allocation: QuestionAllocation): String {
        val constraints = constraintComputer.compute(allocation.questionType)
        return when (allocation.questionType) {
            QuestionType.MC  -> buildMcSchema(allocation, constraints as ComputedConstraints.ForMC)
            QuestionType.MS  -> buildMsSchema(allocation, constraints as ComputedConstraints.ForMS)
            QuestionType.TF  -> buildTfSchema(allocation, constraints as ComputedConstraints.ForTF)
            QuestionType.FIB -> buildFibSchema(allocation, constraints as ComputedConstraints.ForFIB)
        }
    }

    private fun buildMcSchema(
        allocation: QuestionAllocation,
        c: ComputedConstraints.ForMC
    ): String {
        val needsExplanation = allocation.difficulty.needsExplanation()
        val optionsLines = (1..c.totalOptions).joinToString(",\n    ") { "\"option text\"" }

        val schemaFields = buildSchemaFields(
            allocation = allocation,
            coreFields = """
              "$TEXT": "string — the question stem",
              "$TOPIC": "string",
              "$DIFFICULTY": "${allocation.difficulty.name}",
              "$QUESTION_TYPE": "${QuestionType.MC.name}",
              "$OPTIONS": [
                $optionsLines
              ],
              "$CORRECT_OPTION_TEXT": "string — MUST be copied EXACTLY from options, character-for-character"
            """.trimIndent(),
            needsExplanation = needsExplanation
        )

        val explanationRule = explanationRule(needsExplanation)

        return """
            ALLOWED SCHEMA:
            {
            $schemaFields
            }

            HARD RULES — violation = regenerate:
            - $OPTIONS MUST contain EXACTLY ${c.totalOptions} strings
            - $CORRECT_OPTION_TEXT MUST be an exact copy of one value from $OPTIONS
            $explanationRule
        """.trimIndent()
    }

    private fun buildMsSchema(
        allocation: QuestionAllocation,
        c: ComputedConstraints.ForMS
    ): String {
        val needsExplanation = allocation.difficulty.needsExplanation()
        val optionsLines = (1..c.totalOptions).joinToString(",\n    ") { "\"option text\"" }
        val correctOptionsRange = correctOptionsRangeLabel(c.minCorrectOptions, c.maxCorrectOptions)

        val schemaFields = buildSchemaFields(
            allocation = allocation,
            coreFields = """
              "$TEXT": "string — the question stem",
              "$TOPIC": "string",
              "$DIFFICULTY": "${allocation.difficulty.name}",
              "$QUESTION_TYPE": "${QuestionType.MS.name}",
              "$OPTIONS": [
                $optionsLines
              ],
              "$CORRECT_OPTIONS": [
                "exact copy of one option",
                "exact copy of another option"
              ]
            """.trimIndent(),
            needsExplanation = needsExplanation
        )

        val explanationRule = explanationRule(needsExplanation)

        return """
            ALLOWED SCHEMA:
            {
            $schemaFields
            }

            HARD RULES — violation = regenerate:
            - $OPTIONS MUST contain EXACTLY ${c.totalOptions} strings
            - $CORRECT_OPTIONS MUST contain AT LEAST ${c.minCorrectOptions} values
            - $CORRECT_OPTIONS MUST contain AT MOST ${c.maxCorrectOptions} values
            - Allowed correctOptions.size values: $correctOptionsRange
            - At least ${c.minDistractors} option(s) MUST be incorrect (distractors)
            - Each value in $CORRECT_OPTIONS MUST be copied EXACTLY from $OPTIONS — character-for-character
            - NEVER put a string in $CORRECT_OPTIONS that does not appear verbatim in $OPTIONS
            - SINGLE correct option = INVALID — regenerate immediately
            $explanationRule
        """.trimIndent()
    }

    private fun buildTfSchema(
        allocation: QuestionAllocation,
        c: ComputedConstraints.ForTF
    ): String {
        val needsExplanation = allocation.difficulty.needsExplanation()

        val schemaFields = buildSchemaFields(
            allocation = allocation,
            coreFields = """
              "$TEXT": "string — a statement that is either true or false",
              "$TOPIC": "string",
              "$DIFFICULTY": "${allocation.difficulty.name}",
              "$QUESTION_TYPE": "${QuestionType.TF.name}",
              "$OPTIONS": ["True", "False"],
              "$CORRECT_OPTION_TEXT": "True" or "False"
            """.trimIndent(),
            needsExplanation = needsExplanation
        )

        val explanationRule = explanationRule(needsExplanation)

        return """
            ALLOWED SCHEMA:
            {
            $schemaFields
            }

            HARD RULES — violation = regenerate:
            - $OPTIONS MUST be exactly ["True", "False"]
            - $CORRECT_OPTION_TEXT MUST be either "True" or "False"
            $explanationRule

            BALANCE MANDATE — ${c.minimumFalsePercent}% FALSE RULE:
            - At least ${c.minimumFalsePercent}% of TF questions you generate must have correctOption = "False"
            - In a set of ${c.exampleBatchSize} questions: at least ${c.minFalseInExampleBatch} MUST be FALSE
            - This is a HARD constraint, not a suggestion
            - You MUST deliberately craft plausible-but-incorrect statements for the FALSE questions
            - Do NOT default to True — actively decide each question's truth value before writing the stem
            - If you have not yet generated a FALSE question, your NEXT question MUST be FALSE
        """.trimIndent()
    }

    private fun buildFibSchema(
        allocation: QuestionAllocation,
        c: ComputedConstraints.ForFIB
    ): String {
        val needsExplanation = allocation.difficulty.needsExplanation()

        val schemaFields = buildSchemaFields(
            allocation = allocation,
            coreFields = """
              "$TEXT": "string containing exactly one ___ placeholder",
              "$TOPIC": "string",
              "$DIFFICULTY": "${allocation.difficulty.name}",
              "$QUESTION_TYPE": "${QuestionType.FIB.name}",
              "$ACCEPTABLE_ANSWERS": [
                "answer 1",
                "answer 2"
              ]
            """.trimIndent(),
            needsExplanation = needsExplanation
        )

        val explanationRule = explanationRule(needsExplanation)

        return """
            ALLOWED SCHEMA:
            {
            $schemaFields
            }

            HARD RULES — violation = regenerate:
            - $TEXT MUST contain exactly ONE occurrence of "___"
            - $ACCEPTABLE_ANSWERS MUST contain AT LEAST ${c.minAcceptableAnswers} values — SINGLE ANSWER = INVALID
            - $ACCEPTABLE_ANSWERS MUST contain AT MOST ${c.maxAcceptableAnswers} values
            - Every answer in $ACCEPTABLE_ANSWERS MUST:
                (a) grammatically replace ___ in the sentence without changing any other word
                (b) preserve the original meaning of the sentence
                (c) be 1–5 words maximum
            - Answers MUST be genuine synonyms or equivalent phrasings, not the same word repeated
            - Think: what are ${c.minAcceptableAnswers}–${c.maxAcceptableAnswers} words a student might reasonably write that are all correct?
            $explanationRule
        """.trimIndent()
    }

    /**
     * Assembles the schema fields block, inserting the [EXPLANATION] field in
     * the correct position (after [QUESTION_TYPE], before any answer fields)
     * only when [needsExplanation] is true.
     *
     * The explanation field sits after the question-type metadata and before
     * the answer fields because that mirrors how a human would read the object:
     * "what kind of question is this, what does it explain, then what are the answers."
     */
    private fun buildSchemaFields(
        allocation: QuestionAllocation,
        coreFields: String,
        needsExplanation: Boolean
    ): String {
        if (!needsExplanation) return coreFields.indented()

        // Split at the QUESTION_TYPE line so explanation is inserted immediately after it.
        val lines = coreFields.lines().toMutableList()
        val insertAfter = lines.indexOfFirst { it.contains("\"$QUESTION_TYPE\"") }

        return if (insertAfter == -1) {
            // Fallback: append explanation before the last field if marker not found.
            lines.add(lines.size - 1, "  \"$EXPLANATION\": \"string — explain why the correct answer is correct\"")
            lines.joinToString("\n").indented()
        } else {
            lines.add(insertAfter + 1, "  \"$EXPLANATION\": \"string — explain why the correct answer is correct\",")
            lines.joinToString("\n").indented()
        }
    }

    /**
     * Returns the single HARD RULE line that governs the [EXPLANATION] field.
     *
     * When explanation is required, the rule is a concrete instruction.
     * When it is forbidden, the rule is an explicit prohibition.
     * Either way, the model receives one clear directive — never silence.
     */
    private fun explanationRule(needsExplanation: Boolean): String =
        if (needsExplanation)
            "- $EXPLANATION MUST be present and non-blank — explain why the correct answer is correct"
        else
            "- $EXPLANATION MUST NOT be present — omit this field entirely"

    /**
     * Produces a human-readable label for the allowed correctOptions count range.
     * Examples:  minCorrectOptions=2, maxCorrectOptions=2  →  "2"
     *            minCorrectOptions=2, maxCorrectOptions=3  →  "2 or 3"
     *            minCorrectOptions=2, maxCorrectOptions=4  →  "2, 3, or 4"
     */
    private fun correctOptionsRangeLabel(min: Int, max: Int): String =
        when {
            min == max -> "$min"
            max == min + 1 -> "$min or $max"
            else -> (min..max).joinToString(
                separator = ", ",
                transform = { it.toString() }
            ).let { csv ->
                // Replace last ", " with ", or " for natural English
                val lastComma = csv.lastIndexOf(", ")
                csv.substring(0, lastComma) + ", or " + csv.substring(lastComma + 2)
            }
        }

    /** Indents every line of a trimmed block by 2 spaces for clean JSON appearance. */
    private fun String.indented(spaces: Int = 2): String =
        lines().joinToString("\n") { " ".repeat(spaces) + it }
}

/**
 * Returns true for difficulty levels where an explanation of the correct
 * answer adds meaningful learning value.
 *
 * Kept as an extension on [Difficulty] so the policy lives in one place
 * and can be referenced by both [SchemaBuilder] and [QuestionValidator].
 */
fun Difficulty.needsExplanation(): Boolean = when (this) {
    Difficulty.VERY_EASY,
    Difficulty.EASY   -> false
    Difficulty.MEDIUM,
    Difficulty.HARD,
    Difficulty.VERY_HARD -> true
}