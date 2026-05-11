package com.xapps.question_generator.new_prompt_builder

import com.xapps.model.QuestionType
import com.xapps.question_generator.prompt_builder.ContentOptimizer
import com.xapps.questions.contracts.question_generation.QuestionAllocation
import com.xapps.question_generator.workflow.QuestionSchemaHolder
import org.springframework.stereotype.Component

/**
 * Assembles the complete generation prompt.
 *
 * Responsibility is purely structural assembly — all constraint values
 * are computed by [ConstraintComputer] and injected by [SchemaBuilder].
 * This class never hard-codes a number that belongs in [QuestionTypeConfigRegistry].
 */
@Component
class PromptBuilder(
    private val contentOptimizer: ContentOptimizer,
    private val schemaBuilder: SchemaBuilder,
    private val difficultySection: DifficultySection,
    private val typeConstraintSection: TypeConstraintSection
) : QuestionSchemaHolder() {

    fun build(
        content: String,
        allocation: QuestionAllocation
    ): String {
        val optimizedContent = contentOptimizer.optimizeContent(content)
        val schema = schemaBuilder.buildSchema(allocation)
        val difficulty = difficultySection.build(allocation.difficulty, allocation.questionType)
        val typeConstraints = typeConstraintSection.build(allocation)

        return """
            You are a STRICT QUESTION GENERATION ENGINE.

            You operate in 3 internal phases:
            (1) PLAN → (2) GENERATE → (3) VALIDATE

            You MUST complete ALL phases before output.
            Any violation of HARD RULES = INVALID OUTPUT → regenerate that question.

            ================================================
            INPUT CONFIGURATION
            ================================================

            Question Type: ${allocation.questionType.name}
            Difficulty: ${allocation.difficulty.name}
            Required Count: ${allocation.count}

            ================================================
            SOURCE CONTENT (ONLY TRUTH SOURCE)
            ================================================

            You MUST use ONLY this content. You MUST NOT use external knowledge.

            $optimizedContent

            ================================================
            SCHEMA CONTRACT (STRICT)
            ================================================

            $schema

            ================================================
            GLOBAL INVARIANTS
            ================================================

            - Generate EXACTLY ${allocation.count} question(s)
            - No duplicates
            - Every question must be answerable from source content only
            - No hallucinated facts
            - Each question tests ONE concept only

            ================================================
            DIFFICULTY ENGINE
            ================================================

            $difficulty

            ================================================
            QUESTION TYPE CONSTRAINTS
            ================================================

            $typeConstraints

            ================================================
            PHASE 1: PLAN
            ================================================

            Before writing any question:
            - Map each question to a distinct concept from the content
            - Confirm the difficulty rule is satisfiable for that concept
            - For TF: explicitly decide True or False FIRST, then write the stem
            - For MS: count how many correct options you will mark BEFORE writing options
            - For FIB: list all acceptable synonyms BEFORE writing the blank sentence

            ================================================
            PHASE 2: GENERATE
            ================================================

            Generate each question one-by-one:
            - Fully complete, schema-compliant object
            - No placeholders

            ================================================
            PHASE 3: VALIDATE (HARD GATE)
            ================================================

            Before output, check every question:

            FOR ALL QUESTIONS:
            - Schema fields match exactly — no extra fields, no missing fields
            - Answerable from content only

            ${
                when(allocation.questionType) {
                    QuestionType.MC -> Unit
                    QuestionType.MS -> {
                        """
                            FOR MS:
                                - correctOptions.size ≥ 2 — single correct option = FAIL → regenerate
                                - Every correctOptions value exists verbatim in options
                        """.trimIndent()
                    }
                    QuestionType.TF -> {
                        """
                            FOR TF:
                                - Not all correctOption values are "True" — check the FALSE ratio

                        """.trimIndent()
                    }
                    QuestionType.FIB -> {
                        """
                            FOR FIB:
                                - Exactly one ___ in text
                                - acceptableAnswers.size ≥ 2 — single answer = FAIL → regenerate
                        """.trimIndent()
                    }
                }       
            }
            
            FOR DIFFICULTY:
            - Matches cognitive rules stated above

            ON FAILURE: regenerate ONLY the failed question(s)

            ================================================
            OUTPUT FORMAT
            ================================================

            Return ONLY valid JSON:

            {
              "$QUESTIONS": [
                ${fixedSlots(allocation.count)}
              ]
            }

            Do not explain. Do not summarize. No text outside JSON.
        """.trimIndent()
    }

    private fun fixedSlots(count: Int): String =
        (1..count).joinToString(",\n") { """{ "__SLOT__": $it }""" }
}