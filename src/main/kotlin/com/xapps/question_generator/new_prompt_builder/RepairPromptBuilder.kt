package com.xapps.question_generator.new_prompt_builder

import com.xapps.question_generator.workflow.QuestionSchemaHolder
import com.xapps.question_generator.workflow.questions_json_parser.QuestionsJsonParser.FailedQuestion
import com.xapps.questions.contracts.question_generation.QuestionAllocation
import kotlinx.serialization.json.JsonObject
import org.springframework.stereotype.Component

/**
 * Builds a targeted repair prompt for questions that failed structural validation.
 *
 * Rather than re-running the full generation prompt (which produced invalid output once),
 * this prompt tells the model exactly which questions are broken and exactly what is wrong —
 * minimising the chance the model repeats the same mistake.
 */
@Component
class RepairPromptBuilder(
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

            The following question(s) were generated but failed structural validation.
            You MUST fix ONLY the listed issues and return corrected questions.

            ================================================
            ORIGINAL SCHEMA (still applies)
            ================================================

            $schema

            ================================================
            QUESTIONS TO REPAIR
            ================================================

            $failureBlock

            ================================================
            REPAIR RULES
            ================================================

            - Return ONLY the repaired questions — do not add new ones
            - Fix EXACTLY the issues listed for each question
            - Do not change the question topic or difficulty
            - Do not change the question text unless it is part of the reported failure
            - Each repaired question MUST fully comply with the schema above

            ================================================
            OUTPUT FORMAT
            ================================================

            Return ONLY valid JSON:

            {
              "$QUESTIONS": [
                ${fixedSlots(failed.size)}
              ]
            }

            Do not explain. Do not summarize. No text outside JSON.
        """.trimIndent()
    }

    private fun buildFailureBlock(failed: List<FailedQuestion>): String =
        failed.joinToString("\n\n") { failedQuestion ->
            buildString {
                appendLine("--- Question $failedQuestion ---")
                appendLine("Original output:")
                appendLine(failedQuestion.raw.prettyPrint())
                appendLine("Validation failures:")
                failedQuestion.failures.forEach { failure ->
                    appendLine("  • $failure")
                }
            }
        }

    private fun JsonObject.prettyPrint(): String =
        entries.joinToString(prefix = "{\n", postfix = "\n}", separator = ",\n") { (k, v) ->
            "  \"$k\": $v"
        }

    private fun fixedSlots(count: Int): String =
        (1..count).joinToString(",\n") { """{ "__SLOT__": $it }""" }
}