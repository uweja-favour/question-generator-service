package com.xapps.question_generator.new_prompt_builder

import com.xapps.model.QuestionType
import org.springframework.stereotype.Component
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Translates [QuestionTypeConfigRegistry] values into concrete numbers
 * that can be injected verbatim into prompts and validation logic.
 *
 * Centralising this computation means PromptBuilder and QuestionValidator
 * both derive their numbers from one place — they can never drift apart.
 */
@Component
class ConstraintComputer(
    private val registry: QuestionTypeConfigRegistry
) {

    /**
     * Returns a [ComputedConstraints] object appropriate for the given [QuestionType].
     * Callers downcast to the specific subtype they need.
     */
    fun compute(questionType: QuestionType): ComputedConstraints =
        when (val constraints = registry.configFor(questionType).constraints) {
            is QuestionConstraints.MultipleChoice -> ComputedConstraints.ForMC(
                totalOptions = constraints.totalOptions
            )
            is QuestionConstraints.MultipleSelect -> ComputedConstraints.ForMS(
                totalOptions = constraints.totalOptions,
                minCorrectOptions = constraints.minCorrectOptions,
                maxCorrectOptions = constraints.maxCorrectOptions,
                minDistractors = constraints.totalOptions - constraints.maxCorrectOptions
            )
            is QuestionConstraints.TrueFalse -> {
                // Pre-compute how many false questions are needed in a batch of N.
                // Prompt uses this as a concrete rule: "for every 5 TF questions, at least 2 must be FALSE"
                val batchSize = 5
                val minFalseInBatch = ceil(batchSize * constraints.minimumFalseRatio).roundToInt()
                ComputedConstraints.ForTF(
                    minimumFalseRatio = constraints.minimumFalseRatio,
                    minimumFalsePercent = (constraints.minimumFalseRatio * 100).roundToInt(),
                    exampleBatchSize = batchSize,
                    minFalseInExampleBatch = minFalseInBatch
                )
            }
            is QuestionConstraints.FillInTheBlank -> ComputedConstraints.ForFIB(
                minAcceptableAnswers = constraints.minAcceptableAnswers,
                maxAcceptableAnswers = constraints.maxAcceptableAnswers
            )
        }
}

/**
 * Computed, prompt-ready constraint values for each question type.
 * Immutable value objects — safe to pass through the prompt building pipeline.
 */
sealed interface ComputedConstraints {

    data class ForMC(
        val totalOptions: Int
    ) : ComputedConstraints

    data class ForMS(
        val totalOptions: Int,
        val minCorrectOptions: Int,
        val maxCorrectOptions: Int,
        /** Minimum number of wrong options that must always be present. */
        val minDistractors: Int
    ) : ComputedConstraints

    data class ForTF(
        val minimumFalseRatio: Double,
        /** e.g. 40 → "at least 40% of answers must be FALSE" */
        val minimumFalsePercent: Int,
        /** Reference batch size used for the concrete example in the prompt */
        val exampleBatchSize: Int,
        /** Concrete count shown in prompt: "at least N out of exampleBatchSize must be FALSE" */
        val minFalseInExampleBatch: Int
    ) : ComputedConstraints

    data class ForFIB(
        val minAcceptableAnswers: Int,
        val maxAcceptableAnswers: Int
    ) : ComputedConstraints
}