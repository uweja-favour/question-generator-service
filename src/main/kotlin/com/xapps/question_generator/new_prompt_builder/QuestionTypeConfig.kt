package com.xapps.question_generator.new_prompt_builder

import com.xapps.model.QuestionType

/**
 * Single source of truth for all structural constraints per question type.
 *
 * Changing any value here automatically propagates through ConstraintComputer,
 * PromptBuilder, and QuestionValidator — no other files need touching.
 */
data class QuestionTypeConfig(
    val questionType: QuestionType,
    val constraints: QuestionConstraints
)

/**
 * Structural limits that govern how a question of a given type is shaped.
 * All downstream components derive their behaviour from these values.
 */
sealed interface QuestionConstraints {

    data class MultipleChoice(
        val totalOptions: Int = 4
    ) : QuestionConstraints

    data class MultipleSelect(
        val totalOptions: Int = 4,
        /**
         * Minimum number of options that must be marked correct.
         * Must be ≥ 2 to ensure genuine multi-select semantics.
         */
        val minCorrectOptions: Int = 2,
        /**
         * Maximum correct options. Must be < totalOptions so at least
         * one distractor is always present.
         */
        val maxCorrectOptions: Int = 3
    ) : QuestionConstraints {
        init {
            require(minCorrectOptions >= 2) {
                "MS questions must have at least 2 correct options, got minCorrectOptions=$minCorrectOptions"
            }
            require(maxCorrectOptions < totalOptions) {
                "MS questions must have at least 1 distractor; " +
                        "maxCorrectOptions=$maxCorrectOptions must be < totalOptions=$totalOptions"
            }
            require(minCorrectOptions <= maxCorrectOptions) {
                "minCorrectOptions=$minCorrectOptions must be ≤ maxCorrectOptions=$maxCorrectOptions"
            }
        }
    }

    data class TrueFalse(
        /**
         * Minimum fraction of TF questions that must have a FALSE answer.
         * Expressed as a value in (0.0, 1.0). Default 0.4 = at least 40% false.
         */
        val minimumFalseRatio: Double = 0.4
    ) : QuestionConstraints {
        init {
            require(minimumFalseRatio in 0.01..0.99) {
                "minimumFalseRatio must be between 0.01 and 0.99, got $minimumFalseRatio"
            }
        }
    }

    data class FillInTheBlank(
        /**
         * Minimum number of acceptable answers that must be supplied.
         * ≥ 2 ensures the model provides genuine synonyms/alternatives.
         */
        val minAcceptableAnswers: Int = 2,
        val maxAcceptableAnswers: Int = 5
    ) : QuestionConstraints {
        init {
            require(minAcceptableAnswers >= 2) {
                "FIB questions must have at least 2 acceptable answers, got $minAcceptableAnswers"
            }
            require(maxAcceptableAnswers >= minAcceptableAnswers) {
                "maxAcceptableAnswers=$maxAcceptableAnswers must be ≥ minAcceptableAnswers=$minAcceptableAnswers"
            }
        }
    }
}