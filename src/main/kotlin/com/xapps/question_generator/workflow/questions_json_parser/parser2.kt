package com.xapps.question_generator.workflow.questions_json_parser

import com.xapps.question_generator.new_prompt_builder.QuestionValidator
import com.xapps.question_generator.workflow.QuestionSchemaHolder
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.QuestionParser
import com.xapps.question_generator.new_prompt_builder.TfBalanceTracker
import com.xapps.question_generator.new_prompt_builder.ValidationResult
import com.xapps.questions.contracts.question_generation.QuestionAllocation
import com.xapps.questions.contracts.self_test_generation.dto.QuestionDTO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class QuestionsJsonParser(
    private val questionParser: QuestionParser,
    private val questionValidator: QuestionValidator,
    private val tfBalanceTracker: TfBalanceTracker
) : QuestionSchemaHolder() {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val parseSemaphore = Semaphore(15)

    /**
     * Represents a question that failed structural validation, along with
     * the reasons it failed. Passed to the repair callback so the caller
     * can build a targeted repair prompt.
     */
    data class FailedQuestion(
        val raw: JsonObject,
        val failures: List<String>
    )

    /**
     * Parses the LLM JSON response for a given allocation.
     *
     * Valid questions are returned immediately. Invalid questions are collected
     * and passed to [onRepairNeeded], whose results are merged into the final list.
     *
     * @param onRepairNeeded Suspend callback invoked with the list of failed questions.
     *   The caller is responsible for re-prompting and returning repaired [QuestionDTO]s.
     */
    suspend fun parse(
        questionsJson: JsonObject,
        allocation: QuestionAllocation,
        onRepairNeeded: suspend (List<FailedQuestion>) -> List<QuestionDTO>
    ): List<QuestionDTO> = coroutineScope {

        val rawQuestions = extractRawQuestions(questionsJson)

        // Validate and parse concurrently
        val results: List<ParsedOutcome> = rawQuestions
            .mapIndexed { index, element ->
                async {
                    parseSemaphore.withPermit {
                        processSingleQuestion(element.jsonObject, allocation)
                    }
                }
            }
            .awaitAll()

        val valid = results.filterIsInstance<ParsedOutcome.Valid>().map { it.dto }
        val failed = results.filterIsInstance<ParsedOutcome.Invalid>().map { it.failed }

        val repaired = if (failed.isNotEmpty()) onRepairNeeded(failed) else emptyList()

        valid + repaired
    }

    /**
     * Parses repaired questions returned from a repair prompt.
     * Does not trigger further repair — invalid repaired questions are logged and dropped.
     */
    suspend fun parseRepaired(
        questionsJson: JsonObject,
        allocation: QuestionAllocation
    ): List<QuestionDTO> = coroutineScope {

        val rawQuestions = extractRawQuestions(questionsJson)

        rawQuestions
            .map { element ->
                async {
                    parseSemaphore.withPermit {
                        processSingleQuestion(element.jsonObject, allocation)
                    }
                }
            }
            .awaitAll()
            .mapNotNull { outcome ->
                when (outcome) {
                    is ParsedOutcome.Valid -> outcome.dto
                    is ParsedOutcome.Invalid -> {
                        logger.warn(
                            "Repaired question at index ${outcome.failed} still invalid: " +
                                    outcome.failed.failures.joinToString("; ")
                        )
                        null
                    }
                }
            }
    }


    private fun extractRawQuestions(questionsJson: JsonObject): JsonArray =
        questionsJson[QUESTIONS]?.jsonArray
            ?: error("Missing or invalid '$QUESTIONS' array in JSON response.")

    private fun processSingleQuestion(
        obj: JsonObject,
        allocation: QuestionAllocation
    ): ParsedOutcome {

        val type = requireNotNull(JsonQuestionInspector.detectType(obj)) {
            "Missing 'questionType' for question $obj."
        }
        val difficulty = requireNotNull(JsonQuestionInspector.detectDifficulty(obj)) {
            "Missing 'difficulty' for question $obj."
        }

        return when (val result = questionValidator.validate(obj, type, difficulty)) {
            is ValidationResult.Valid -> {
                val dto = questionParser.parse(type, obj, difficulty)
                // Record TF answers for balance tracking
                tfBalanceTracker.record(allocation.questionType, obj)
                ParsedOutcome.Valid(dto)
            }
            is ValidationResult.Invalid -> {
                logger.warn("Question $obj failed validation: ${result.summary()}")
                ParsedOutcome.Invalid(
                    FailedQuestion(raw = obj, failures = result.failures)
                )
            }
        }
    }

    private sealed interface ParsedOutcome {
        data class Valid(val dto: QuestionDTO) : ParsedOutcome
        data class Invalid(val failed: FailedQuestion) : ParsedOutcome
    }
}