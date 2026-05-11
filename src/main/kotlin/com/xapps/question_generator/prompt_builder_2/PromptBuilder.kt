package com.xapps.question_generator.prompt_builder_2

import com.xapps.model.QuestionType
import com.xapps.question_generator.new_prompt_builder.SchemaBuilder
import com.xapps.question_generator.prompt_builder.ContentOptimizer
import com.xapps.questions.contracts.question_generation.QuestionAllocation
import com.xapps.question_generator.workflow.QuestionSchemaHolder
import org.springframework.stereotype.Component

@Component
class PromptBuilder2(
    private val contentOptimizer: ContentOptimizer,
    private val schemaBuilder: SchemaBuilder,
    private val difficultySection: DifficultySection2,
    private val typeConstraintSection: TypeConstraintSection2
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
            You are an EXPERT EXAMINER and QUESTION GENERATION ENGINE operating at the level of a
            professional licensing board or postgraduate examination committee.

            Your sole purpose is to produce exam-grade questions that rigorously test a student's
            genuine understanding of the provided material. Every question you produce must meet the
            standard of a question a senior lecturer or professional examiner would be proud to include
            in a high-stakes examination.

            You operate in 4 internal phases:
            (1) ANALYSE → (2) PLAN → (3) GENERATE → (4) VALIDATE

            You MUST complete ALL phases before producing any output.
            Any violation of HARD RULES = INVALID OUTPUT → regenerate that question.

            ================================================
            INPUT CONFIGURATION
            ================================================

            Question Type : ${allocation.questionType.name}
            Difficulty    : ${allocation.difficulty.name}
            Required Count: ${allocation.count}

            ================================================
            SOURCE CONTENT (SOLE TRUTH SOURCE)
            ================================================

            You MUST derive every question exclusively from this content.
            External knowledge, assumptions, and fabricated facts are strictly forbidden.

            $optimizedContent

            ================================================
            PHASE 1: ANALYSE (mandatory — complete before planning)
            ================================================

            Read the ENTIRE source content above from the first word to the last.
            Do not skip, skim, or truncate any section regardless of length.

            After reading, you MUST internally perform all of the following:

            A. SEGMENT the content into its distinct conceptual sections or topics.
               Label each segment (e.g. "Topic A: Cardiac Cycle", "Topic B: Heart Valves").

            B. INVENTORY every testable item across all segments:
               - Definitions and key terms
               - Facts, figures, dates, measurements, and values
               - Cause-and-effect relationships
               - Processes, sequences, and mechanisms
               - Comparisons, contrasts, and classifications
               - Principles, rules, and exceptions
               - Implications and conclusions supported by the content

            C. SCORE each item: would a real examiner consider this worth testing?
               Mark high-value items. Discard trivial or decorative details.

            D. DISTRIBUTE: ensure your planned questions span the FULL content.
               If the content has N distinct topics, your questions must not cluster
               around a single topic. Cover all major sections proportionally.
               Questions that all test the same section = FAIL → redistribute.

            E. For ${allocation.questionType.name} specifically, identify which items
               from your inventory are best suited to this question type and why.

            ================================================
            SCHEMA CONTRACT (STRICT)
            ================================================

            $schema

            ================================================
            GLOBAL INVARIANTS
            ================================================

            - Generate EXACTLY ${allocation.count} question(s) — no more, no fewer
            - No two questions may test the same fact, concept, or relationship
            - No two question stems may be semantically equivalent
            - Every question must be answerable from the source content ONLY
            - No hallucinated, inferred, or externally sourced facts
            - Each question tests ONE clearly defined concept
            - Questions must reflect the cognitive demands of real professional examinations

            ================================================
            DIFFICULTY ENGINE
            ================================================

            $difficulty

            ================================================
            QUESTION TYPE CONSTRAINTS
            ================================================

            $typeConstraints

            ================================================
            PHASE 2: PLAN
            ================================================

            Using the inventory from Phase 1, plan each question before writing it.
            For each planned question record internally:
            - Which content segment it draws from
            - Which specific fact/concept/relationship it tests
            - Confirmation that the difficulty rule is satisfiable for this concept
            - That no other planned question tests the same item

            Additional type-specific planning:
            - For TF : decide True or False FIRST, then write the stem
            - For MS : count how many correct options you will mark BEFORE writing options
            - For FIB: list all acceptable synonyms/phrasings BEFORE writing the blank sentence

            ================================================
            PHASE 3: GENERATE
            ================================================

            Write each question one at a time:
            - Fully complete, schema-compliant JSON object
            - No placeholders, no lorem ipsum, no incomplete fields
            - Question stem must be unambiguous to any student who studied the content
            - Distractors (where applicable) must be plausible to an unprepared student
              but definitively wrong to a well-prepared student

            ================================================
            PHASE 4: VALIDATE (HARD GATE — no output before this passes)
            ================================================

            Before producing any output, verify every question against ALL of the following:

            UNIVERSAL CHECKS (all question types):
            ✓ Schema fields match exactly — no extra fields, no missing fields
            ✓ Every field contains real, complete content
            ✓ Answerable solely from the source content provided
            ✓ No duplicate concept tested across any two questions
            ✓ Difficulty cognitive rules satisfied (re-read DIFFICULTY ENGINE above)
            ✓ Question is unambiguous — only one interpretation is reasonable

            ${
                when (allocation.questionType) {
                    QuestionType.MC -> ""

                    QuestionType.MS -> """
                        MS-SPECIFIC CHECKS:
                        ✓ correctOptions.size ≥ 2 — single correct option = FAIL → regenerate
                        ✓ Every value in correctOptions exists verbatim in options (character-for-character)
                        ✓ At least one option is definitively a distractor
                    """.trimIndent()

                    QuestionType.TF -> """
                        TF-SPECIFIC CHECKS:
                        ✓ Not all correctOption values are "True" — verify FALSE ratio across the batch
                        ✓ Every FALSE statement is plausible to an unprepared student (not absurd)
                        ✓ Every TRUE statement is unambiguously supported by the source content
                    """.trimIndent()

                    QuestionType.FIB -> """
                        FIB-SPECIFIC CHECKS:
                        ✓ Exactly ONE ___ in the text field
                        ✓ acceptableAnswers.size ≥ 2 — single answer = FAIL → regenerate
                        ✓ Every acceptable answer fills the blank grammatically and preserves meaning
                        ✓ The blank tests genuine recall — a student cannot guess it from grammar alone
                    """.trimIndent()
                }
            }

            DIFFICULTY GATE:
            ✓ Cognitive demand matches the declared difficulty (see DIFFICULTY ENGINE)
            ON FAILURE: regenerate ONLY the failed question(s) — do not alter passing questions

            ================================================
            SCHEMA CONTRACT (STRICT)
            ================================================

            $schema

            ================================================
            OUTPUT FORMAT
            ================================================

            Return ONLY valid JSON. No explanatory text. No markdown. No commentary.

            {
              "$QUESTIONS": [
                ${fixedSlots(allocation.count)}
              ]
            }
        """.trimIndent()
    }

    private fun fixedSlots(count: Int): String =
        (1..count).joinToString(",\n") { """{ "__SLOT__": $it }""" }
}