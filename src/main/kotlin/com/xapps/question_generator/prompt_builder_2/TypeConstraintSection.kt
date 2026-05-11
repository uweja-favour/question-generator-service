package com.xapps.question_generator.prompt_builder_2

import com.xapps.model.QuestionType
import com.xapps.question_generator.new_prompt_builder.ComputedConstraints
import com.xapps.question_generator.new_prompt_builder.ConstraintComputer
import com.xapps.questions.contracts.question_generation.QuestionAllocation
import org.springframework.stereotype.Component

@Component
class TypeConstraintSection2(
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
        - Exactly 1 option is correct
        - Distractors MUST be plausible to an underprepared student but unambiguously wrong
          to a student who thoroughly studied the content
        - Avoid "all of the above" and "none of the above" constructions
        - Avoid negative stems ("Which of the following is NOT...") unless the difficulty demands it
    """.trimIndent()

    private fun buildMs(c: ComputedConstraints.ForMS): String = """
        MS (Multiple Select) — READ CAREFULLY
        - Provide exactly ${c.totalOptions} options
        - Mark BETWEEN ${c.minCorrectOptions} AND ${c.maxCorrectOptions} options as correct (inclusive)
        - You MUST mark at least ${c.minCorrectOptions} correct options — 1 correct option is INVALID
        - You MUST leave at least ${c.minDistractors} option(s) as incorrect distractors
        - Vary the number of correct options across questions — do not always use the minimum
        - Each value in correctOptions MUST match an option string character-for-character
        - Distractors must be plausible — not obviously wrong, not trivially distinguishable
        - A well-prepared student should be able to identify all correct options with confidence
    """.trimIndent()

    private fun buildTf(c: ComputedConstraints.ForTF): String = """
        TF (True/False) — READ CAREFULLY
        - Write a declarative STATEMENT, not a question
        - correctOption MUST be exactly "True" or "False"
        - MANDATORY: at least ${c.minimumFalsePercent}% of TF questions must be FALSE
        - In a batch of ${c.exampleBatchSize}: at least ${c.minFalseInExampleBatch} must be FALSE

        HOW TO WRITE EXAM-GRADE TRUE STATEMENTS:
        - Derive directly from an unambiguous fact in the content
        - Must be verifiably correct — not merely plausible
        - A student who studied thoroughly should confirm it with full confidence

        HOW TO WRITE EXAM-GRADE FALSE STATEMENTS:
        - Start from a TRUE fact in the content
        - Alter ONE specific detail to make it false: swap a number, reverse a relationship,
          substitute a term with a plausible but incorrect alternative, invert a direction or order
        - The altered statement must be PLAUSIBLE to an unprepared student
        - The alteration must be DETECTABLE only by a student who knows the material
        - NEVER write absurd, obviously impossible, or self-evidently wrong statements
        - NEVER fabricate a fact not present in the source content

        BALANCE MANDATE:
        - BEFORE writing each statement, decide: True or False?
        - Track your running count. If you have not yet written a FALSE question, your NEXT must be FALSE.
        - In a set of ${c.exampleBatchSize} questions: at least ${c.minFalseInExampleBatch} MUST be FALSE
    """.trimIndent()

    private fun buildFib(c: ComputedConstraints.ForFIB): String = """
        FIB (Fill in the Blank) — READ CAREFULLY

        PURPOSE: Test whether a student can recall a specific term, value, name, process,
        relationship, or concept from the material — not whether they can pattern-match a sentence.

        WHAT MAKES A GOOD FIB QUESTION:
        - The blank replaces the KEY piece of information the question is testing
        - A student who knows the material fills it immediately and confidently
        - A student who does not know the material cannot guess it from grammar or context
        - The completed sentence reads naturally and is unambiguous

        WHAT MAKES A BAD FIB QUESTION (these are forbidden):
        - Copying a sentence verbatim from the notes and removing a random word
        - Blanking out a word that is guessable from the sentence's grammatical structure
        - Blanking out a trivial or decorative word (articles, prepositions, filler phrases)
        - Creating a blank where multiple unrelated words could plausibly fit

        HOW TO CONSTRUCT:
        1. Identify a high-value testable item: a term, value, mechanism, relationship, or fact
        2. Write a clear, natural statement about it
        3. Replace the key item with ___
        4. Confirm: could a student guess the answer without having studied? If yes — rewrite

        STRUCTURAL RULES:
        - text MUST contain exactly ONE ___ placeholder
        - acceptableAnswers MUST contain AT LEAST ${c.minAcceptableAnswers} values — SINGLE ANSWER = INVALID
        - acceptableAnswers MUST contain AT MOST ${c.maxAcceptableAnswers} values
        - Every value in acceptableAnswers MUST:
            (a) fill the blank grammatically without changing any other word in the sentence
            (b) preserve the original meaning of the complete statement
            (c) be 1–5 words in length
        - Acceptable answers are genuine synonyms, equivalent phrasings, or accepted abbreviations
          — not the same word repeated with different capitalisation
        - Think: "what are ${c.minAcceptableAnswers}–${c.maxAcceptableAnswers} phrasings
          a knowledgeable student might correctly write here?"
    """.trimIndent()
}