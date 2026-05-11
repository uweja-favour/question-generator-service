package com.xapps.question_generator.new_prompt_builder

import com.xapps.model.QuestionType
import org.springframework.stereotype.Component

/**
 * Registry that maps each [QuestionType] to its active [QuestionTypeConfig].
 *
 * To change constraints for any question type — e.g. increase MS options from 4 to 6 —
 * edit only this file. Everything downstream (prompt building, validation) adapts automatically.
 */
@Component
class QuestionTypeConfigRegistry {

    private val registry: Map<QuestionType, QuestionTypeConfig> = mapOf(
        QuestionType.MC to QuestionTypeConfig(
            questionType = QuestionType.MC,
            constraints = QuestionConstraints.MultipleChoice(
                totalOptions = 4
            )
        ),
        QuestionType.MS to QuestionTypeConfig(
            questionType = QuestionType.MS,
            constraints = QuestionConstraints.MultipleSelect(
                totalOptions = 4,
                minCorrectOptions = 2,
                maxCorrectOptions = 3
            )
        ),
        QuestionType.TF to QuestionTypeConfig(
            questionType = QuestionType.TF,
            constraints = QuestionConstraints.TrueFalse(
                minimumFalseRatio = 0.4
            )
        ),
        QuestionType.FIB to QuestionTypeConfig(
            questionType = QuestionType.FIB,
            constraints = QuestionConstraints.FillInTheBlank(
                minAcceptableAnswers = 2,
                maxAcceptableAnswers = 5
            )
        )
    )

    fun configFor(questionType: QuestionType): QuestionTypeConfig =
        registry[questionType]
            ?: error("No QuestionTypeConfig registered for type: $questionType")
}