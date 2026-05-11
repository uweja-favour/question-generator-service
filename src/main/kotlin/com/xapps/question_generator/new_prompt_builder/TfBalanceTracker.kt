package com.xapps.question_generator.new_prompt_builder

import com.xapps.model.QuestionType
import com.xapps.question_generator.workflow.QuestionSchemaHolder
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.stringOrNull
import kotlinx.serialization.json.JsonObject
import org.springframework.stereotype.Component
import kotlin.math.ceil

/**
 * Tracks the True/False answer balance across all TF questions generated
 * within a single [AllocationQuestionGenerator.generate] call.
 *
 * Thread-safety: this component is used within structured coroutines scoped
 * to a single generation job. [reset] must be called at the start of each job.
 * If parallel TF allocations are ever introduced, callers should synchronise
 * access or create one tracker instance per job via a factory.
 */
@Component
class TfBalanceTracker(
    private val registry: QuestionTypeConfigRegistry
) : QuestionSchemaHolder() {

    private var trueCount = 0
    private var falseCount = 0

    val total: Int get() = trueCount + falseCount
    val falseRatio: Double get() = if (total == 0) 0.0 else falseCount.toDouble() / total

    /**
     * Records the correct-option value of a parsed TF question.
     * Silently ignores calls for non-TF question types.
     */
    fun record(questionType: QuestionType, rawQuestion: JsonObject) {
        if (questionType != QuestionType.TF) return
        when (rawQuestion.stringOrNull(CORRECT_OPTION_TEXT)?.lowercase()) {
            "true"  -> trueCount++
            "false" -> falseCount++
            null -> error("Unknown correct option text in ${javaClass.simpleName}")
        }
    }

    /**
     * Returns a correction directive string to prepend to the next TF generation
     * prompt if the false-answer ratio has fallen below the configured threshold,
     * or null if balance is acceptable (or no TF questions have been generated yet).
     */
    fun buildCorrectionHint(questionType: QuestionType): String? {
        if (questionType != QuestionType.TF) return null
        if (total == 0) return null

        val constraints = registry.configFor(QuestionType.TF).constraints
                as? QuestionConstraints.TrueFalse ?: return null

        if (falseRatio >= constraints.minimumFalseRatio) return null

        val requiredFalseTotal = ceil(constraints.minimumFalseRatio * total).toInt()
        val deficit = requiredFalseTotal - falseCount

        return """
            CORRECTION REQUIRED — TRUE/FALSE BALANCE:
            So far in this session: $trueCount TRUE, $falseCount FALSE out of $total TF questions.
            Current false ratio: ${String.format("%.0f", falseRatio * 100)}%  (required: ${(constraints.minimumFalseRatio * 100).toInt()}%)
            You MUST generate at least $deficit more FALSE question(s) to restore balance.
            Your NEXT TF question MUST have correctOption = "False".
        """.trimIndent()
    }

    /** Must be called at the start of each generation job. */
    fun reset() {
        trueCount = 0
        falseCount = 0
    }
}