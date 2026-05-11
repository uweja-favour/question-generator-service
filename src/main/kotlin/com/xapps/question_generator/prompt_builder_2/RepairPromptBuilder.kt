package com.xapps.question_generator.prompt_builder_2

import com.xapps.question_generator.new_prompt_builder.SchemaBuilder
import com.xapps.question_generator.workflow.QuestionSchemaHolder
import com.xapps.question_generator.workflow.questions_json_parser.QuestionsJsonParser.FailedQuestion
import com.xapps.questions.contracts.question_generation.QuestionAllocation
import kotlinx.serialization.json.JsonObject
import org.springframework.stereotype.Component

@Component
class RepairPromptBuilder2(
    private val schemaBuilder: SchemaBuilder
) : QuestionSchemaHolder() {

    fun build(
        failed: List<FailedQuestion>,
        allocation: QuestionAllocation
    ): String {
        require(failed.isNotEmpty()) { "Cannot build repair prompt with no failed questions." }

        val schema = schemaBuilder.buildSchema(allocation)
        val failureBlock = buildFailureBlock(failed)

        return """
            You are a STRICT QUESTION REPAIR ENGINE.

            The questions below were generated but failed structural validation.
            You MUST fix ONLY the reported failures and return corrected questions.
            You MUST NOT alter any field that was not identified as a failure.

            ================================================
            SCHEMA (unchanged — still fully applies)
            ================================================

            $schema

            ================================================
            QUESTIONS TO REPAIR
            ================================================

            $failureBlock

            ================================================
            REPAIR RULES
            ================================================

            - Return ONLY the repaired questions — do not generate new ones
            - Fix EXACTLY the issues listed — do not change anything else
            - Do not alter the question topic, concept being tested, or difficulty
            - Do not alter the question stem unless the stem itself is listed as a failure
            - Every repaired question MUST fully comply with the schema above
            - If fixing one field requires adjusting another for consistency (e.g. updating
              correctOptions after changing options), make all necessary consistent changes

            ================================================
            OUTPUT FORMAT
            ================================================

            Return ONLY valid JSON. No explanatory text. No markdown. No commentary.

            {
              "$QUESTIONS": [
                ${fixedSlots(failed.size)}
              ]
            }
        """.trimIndent()
    }

    private fun buildFailureBlock(failed: List<FailedQuestion>): String =
        failed.mapIndexed { index, failedQuestion ->
            buildString {
                appendLine("--- Question ${index + 1} ---")
                appendLine("Original output:")
                appendLine(failedQuestion.raw.prettyPrint())
                appendLine()
                appendLine("Validation failures:")
                failedQuestion.failures.forEach { failure ->
                    appendLine("  • $failure")
                }
            }
        }.joinToString("\n\n")

    private fun JsonObject.prettyPrint(): String =
        entries.joinToString(prefix = "{\n", postfix = "\n}", separator = ",\n") { (k, v) ->
            "  \"$k\": $v"
        }

    private fun fixedSlots(count: Int): String =
        (1..count).joinToString(",\n") { """{ "__SLOT__": $it }""" }
}