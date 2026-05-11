package com.xapps.question_generator.new_prompt_builder

import com.xapps.model.QuestionType
import com.xapps.questions.contracts.question_generation.QuestionAllocation
import org.springframework.stereotype.Component

/**
 * Builds the QUESTION TYPE CONSTRAINTS section of the generation prompt.
 *
 * All numeric values are derived from [ConstraintComputer] — never hard-coded here.
 */
@Component
class TypeConstraintSection(
    private val constraintComputer: ConstraintComputer
) {

    fun build(allocation: QuestionAllocation): String {
        val constraints = constraintComputer.compute(allocation.questionType)
        return when (allocation.questionType) {
            QuestionType.MC  -> buildMc(constraints as ComputedConstraints.ForMC)
            QuestionType.MS  -> buildMs(constraints as ComputedConstraints.ForMS)
            QuestionType.TF  -> buildTf(constraints as ComputedConstraints.ForTF)
            QuestionType.FIB -> buildFib(constraints as ComputedConstraints.ForFIB)
        }
    }

    private fun buildMc(c: ComputedConstraints.ForMC): String = """
        MC (Multiple Choice)
        - Provide exactly ${c.totalOptions} options
        - Exactly 1 correct option
        - Distractors must be plausible but unambiguously wrong
    """.trimIndent()

    private fun buildMs(c: ComputedConstraints.ForMS): String = """
        MS (Multiple Select) — READ CAREFULLY
        - Provide exactly ${c.totalOptions} options
        - Mark BETWEEN ${c.minCorrectOptions} AND ${c.maxCorrectOptions} options as correct (inclusive)
        - You MUST mark at least ${c.minCorrectOptions} correct options — 1 correct option is INVALID
        - You MUST leave at least ${c.minDistractors} option(s) as incorrect distractors
        - Vary between ${c.minCorrectOptions} and ${c.maxCorrectOptions} correct options across questions — do not always pick the minimum
        - Each value in correctOptions MUST match an option string character-for-character
    """.trimIndent()

    private fun buildTf(c: ComputedConstraints.ForTF): String = """
        TF (True/False) — READ CAREFULLY
        - Write a statement, not a question
        - correctOption MUST be "True" or "False"
        - MANDATORY: at least ${c.minimumFalsePercent}% of your TF questions must be FALSE
        - In a batch of ${c.exampleBatchSize}: at least ${c.minFalseInExampleBatch} must be FALSE
        - Strategy for FALSE questions: take a true fact from the content, then flip one key detail
          (swap a number, reverse a relationship, replace a term with a plausible wrong term)
        - Do NOT write trivially absurd false statements — they must be plausible to a student
        - BEFORE writing the stem, decide: is this question TRUE or FALSE?
          Track your count. If you have not yet written a FALSE question, your NEXT question MUST be FALSE.
    """.trimIndent()

    private fun buildFib(c: ComputedConstraints.ForFIB): String = """
        FIB (Fill in the Blank) — READ CAREFULLY
        - Text MUST contain exactly ONE ___ placeholder
        - Derive the sentence from the source content — only remove one keyword/phrase
        - acceptableAnswers MUST contain AT LEAST ${c.minAcceptableAnswers} values — SINGLE ANSWER = INVALID
        - acceptableAnswers MUST contain AT MOST ${c.maxAcceptableAnswers} values
        - All answers must be grammatically interchangeable in the blank without changing meaning
        - Think: "what are ${c.minAcceptableAnswers}–${c.maxAcceptableAnswers} words a knowledgeable student might reasonably write?"
        - Synonyms, near-synonyms, and equivalent short phrases all count
        - Each answer: 1–5 words, no punctuation unless required by the sentence
    """.trimIndent()
}