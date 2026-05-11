package com.xapps.question_generator.workflow.question_generator

import com.xapps.question_generator.new_prompt_builder.RepairPromptBuilder

import com.xapps.platform.core.outcome.ensureActive
import com.xapps.platform.core.outcome.getOrElse
import com.xapps.platform.core.outcome.outcomeOf
import com.xapps.question_generator.claude_service.ClaudeClient
import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import com.xapps.question_generator.new_prompt_builder.PromptBuilder
import com.xapps.question_generator.workflow.questions_json_parser.QuestionsJsonParser
import com.xapps.question_generator.workflow.throwGenerationFailedException
import com.xapps.question_generator.new_prompt_builder.TfBalanceTracker
import com.xapps.questions.contracts.question_generation.QuestionAllocation
import com.xapps.questions.contracts.self_test_generation.dto.QuestionDTO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

interface AllocationQuestionGenerator {
    suspend fun generate(
        job: QuestionCreationJob,
        optimizedContent: String,
        allocations: List<QuestionAllocation>
    ): List<QuestionDTO>
}

@Component
class AllocationQuestionGeneratorImpl(
    private val aiClient: ClaudeClient,
    private val promptBuilder: PromptBuilder,
    private val repairPromptBuilder: RepairPromptBuilder,
    private val questionsJsonParser: QuestionsJsonParser,
    private val tfBalanceTracker: TfBalanceTracker
) : AllocationQuestionGenerator {

    private val log = LoggerFactory.getLogger(javaClass)
    private val allocationSemaphore = Semaphore(4)

    override suspend fun generate(
        job: QuestionCreationJob,
        optimizedContent: String,
        allocations: List<QuestionAllocation>
    ): List<QuestionDTO> = coroutineScope {
        // Reset TF balance tracking at the start of each generation job
        tfBalanceTracker.reset()

        allocations
            .map { allocation ->
                async {
                    generateForAllocation(
                        optimizedContent = optimizedContent,
                        allocation = allocation
                    )
                }
            }
            .awaitAll()
            .flatten()
    }

    private suspend fun generateForAllocation(
        optimizedContent: String,
        allocation: QuestionAllocation
    ): List<QuestionDTO> = allocationSemaphore.withPermit {
        outcomeOf {
            ensureActive()

            val prompt = buildPromptWithBalanceHint(optimizedContent, allocation)
            val json = aiClient.generateJsonResponse(prompt)

            ensureActive()

            questionsJsonParser.parse(
                questionsJson = json,
                allocation = allocation,
                onRepairNeeded = { failedQuestions ->
                    repairFailedQuestions(
                        failed = failedQuestions,
                        allocation = allocation
                    )
                }
            )
        }.getOrElse { error ->
            log.error("Failed to generate questions for allocation $allocation", error)
            throwGenerationFailedException(
                "Failed for allocation ${allocation.questionType} at difficulty ${allocation.difficulty}. " +
                        "Error: ${error.message}"
            )
        }
    }

    /**
     * Prepends a TF balance correction directive when the false-answer ratio
     * has drifted below the configured threshold for this generation session.
     */
    private fun buildPromptWithBalanceHint(
        optimizedContent: String,
        allocation: QuestionAllocation
    ): String {
        val balanceHint = tfBalanceTracker.buildCorrectionHint(allocation.questionType)
        val basePrompt = promptBuilder.build(optimizedContent, allocation)

        return if (balanceHint != null) {
            "$balanceHint\n\n$basePrompt"
        } else {
            basePrompt
        }
    }

    /**
     * Attempts to repair questions that failed structural validation.
     * Called by [QuestionsJsonParser] via callback when it detects invalid questions.
     */
    private suspend fun repairFailedQuestions(
        failed: List<QuestionsJsonParser.FailedQuestion>,
        allocation: QuestionAllocation
    ): List<QuestionDTO> {
        if (failed.isEmpty()) return emptyList()

        log.warn(
            "Repairing ${failed.size} invalid question(s) for allocation " +
                    "${allocation.questionType}/${allocation.difficulty}"
        )

        val repairPrompt = repairPromptBuilder.build(failed, allocation)

        return outcomeOf {
            val repairJson = aiClient.generateJsonResponse(repairPrompt)
            // Parse repaired questions without allowing further repair recursion
            questionsJsonParser.parseRepaired(repairJson, allocation)
        }.getOrElse { error ->
            log.error("Repair attempt failed for allocation $allocation", error)
            emptyList()
        }
    }
}