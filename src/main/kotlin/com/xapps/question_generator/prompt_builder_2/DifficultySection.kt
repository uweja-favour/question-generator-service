package com.xapps.question_generator.prompt_builder_2

import com.xapps.model.Difficulty
import com.xapps.model.QuestionType
import org.springframework.stereotype.Component

@Component
class DifficultySection2 {

    fun build(difficulty: Difficulty, questionType: QuestionType): String = """
        Active difficulty: ${difficulty.name}

        Every question MUST strictly satisfy the cognitive rules for this level.
        A question that does not meet the cognitive demand for this level is INVALID → regenerate.

        ${
            when (difficulty) {
                Difficulty.VERY_EASY -> veryEasy(questionType)
                Difficulty.EASY      -> easy(questionType)
                Difficulty.MEDIUM    -> medium(questionType)
                Difficulty.HARD      -> hard(questionType)
                Difficulty.VERY_HARD -> veryHard(questionType)
            }
        }
    """.trimIndent()

    private fun veryEasy(questionType: QuestionType): String = """
        --------------------------------
        ${Difficulty.VERY_EASY.name} (weight 1.0) — Direct Recall
        --------------------------------
        Cognitive demand: The answer is stated explicitly in a single sentence of the content.
        No rewording, no inference, no synthesis of any kind is required.

        ${
            when (questionType) {
                QuestionType.MC, QuestionType.MS ->
                    "Incorrect options must be obviously wrong or entirely unrelated to the topic."
                QuestionType.TF ->
                    "The statement is lifted almost verbatim from a single sentence in the content."
                QuestionType.FIB ->
                    "The blank removes a single keyword that is explicitly named in one sentence."
            }
        }

        FAIL CONDITIONS:
        - Answer requires reading more than one sentence
        - Answer requires any interpretation or inference
        → REGENERATE

        VALIDATION: answer traceable to a single sentence in the content.
    """.trimIndent()

    private fun easy(questionType: QuestionType): String = """
        --------------------------------
        ${Difficulty.EASY.name} (weight 2.0) — Recall with Light Rewording
        --------------------------------
        Cognitive demand: The answer is derived from one sentence; the question stem
        may lightly paraphrase but does not require combining ideas or reasoning.

        ${
            when (questionType) {
                QuestionType.MC, QuestionType.MS ->
                    "Distractors are plausible but clearly distinguishable by a prepared student."
                QuestionType.TF ->
                    "The statement paraphrases one sentence; truth value is unambiguous."
                QuestionType.FIB ->
                    "The blank may remove a short phrase of 1–3 words from a single sentence."
            }
        }

        FAIL CONDITIONS:
        - Requires combining ideas from multiple sentences
        - Requires inference beyond surface meaning
        → REGENERATE

        VALIDATION: answer traceable to a single sentence (may be paraphrased).
    """.trimIndent()

    private fun medium(questionType: QuestionType): String = """
        --------------------------------
        ${Difficulty.MEDIUM.name} (weight 3.0) — Comprehension and Basic Inference
        --------------------------------
        Cognitive demand: The student must combine information from AT LEAST 2 distinct ideas,
        understand a relationship, or apply basic inference. The answer is NOT directly stated
        in any single sentence.

        ${
            when (questionType) {
                QuestionType.MC, QuestionType.TF ->
                    "The question tests understanding of a relationship or implication, not rote recall."
                QuestionType.MS ->
                    "At least one distractor must be partially correct but incomplete or conditionally wrong."
                QuestionType.FIB ->
                    "The blank represents a concept implied by combining two or more related sentences."
            }
        }

        FAIL CONDITIONS:
        - Answer can be copied from a single sentence
        - No reasoning or synthesis required
        → REGENERATE

        VALIDATION: at least 2 linked ideas from the content are involved in reaching the answer.
    """.trimIndent()

    private fun hard(questionType: QuestionType): String = """
        --------------------------------
        ${Difficulty.HARD.name} (weight 4.0) — Multi-step Reasoning
        --------------------------------
        Cognitive demand: The student must perform multi-step reasoning combining multiple concepts.
        The answer does not appear explicitly in any single sentence; it must be derived.
        A student who only skimmed the content will not be able to answer correctly.

        ${
            when (questionType) {
                QuestionType.MC, QuestionType.TF ->
                    "The question tests application, analysis, or evaluation — not recognition."
                QuestionType.MS ->
                    "Differences between correct and incorrect options are subtle; superficial reading leads to wrong answers."
                QuestionType.FIB ->
                    "The blank represents a derived concept or conclusion — not a word copied from the text."
            }
        }

        FAIL CONDITIONS:
        - Solvable by direct lookup in the content
        - Only one concept from the content is involved
        → REGENERATE

        VALIDATION: requires multi-concept reasoning; answer is derived, not extracted.
    """.trimIndent()

    private fun veryHard(questionType: QuestionType): String = """
        --------------------------------
        ${Difficulty.VERY_HARD.name} (weight 5.0) — Synthesis and Abstraction
        --------------------------------
        Cognitive demand: The student must synthesise across multiple concepts, abstract from
        specific details to a general principle, or reason about implicit relationships.
        The answer is NEVER explicitly stated anywhere in the content.
        Even a well-prepared student must think carefully before answering.

        ${
            when (questionType) {
                QuestionType.MC, QuestionType.TF ->
                    "The question tests evaluation, synthesis, or application to a novel scenario described in the content."
                QuestionType.MS ->
                    "All options appear correct at first glance; only precise understanding of the content distinguishes them."
                QuestionType.FIB ->
                    "The blank represents an abstracted or inferred concept — it cannot be found as a word or phrase in the text."
            }
        }

        FAIL CONDITIONS:
        - Answer is explicitly stated anywhere in the content
        - No reasoning chain is required
        - A student could answer correctly by skimming
        → REGENERATE

        VALIDATION: requires synthesis or abstraction; answer is inferred, never extracted.
    """.trimIndent()
}