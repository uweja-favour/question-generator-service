//package com.xapps.question_generator.workflow.question_generator
//
//import com.xapps.platform.core.outcome.ensureActive
//import com.xapps.platform.core.outcome.getOrElse
//import com.xapps.platform.core.outcome.outcomeOf
//import com.xapps.question_generator.gpt_service.GptClient
//import com.xapps.question_generator.job.domain.model.QuestionCreationJob
//import com.xapps.question_generator.prompt_builder.PromptBuilder
//import com.xapps.question_generator.workflow.throwGenerationFailedException
//import com.xapps.questions.contracts.self_test_generation.dto.QuestionDTO
//import com.xapps.questions.contracts.question_generation.QuestionAllocation
//import kotlinx.coroutines.async
//import kotlinx.coroutines.awaitAll
//import kotlinx.coroutines.coroutineScope
//import kotlinx.coroutines.sync.Semaphore
//import kotlinx.coroutines.sync.withPermit
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Component
//import kotlin.collections.map
//
//interface AllocationQuestionGenerator {
//    suspend fun generate(
//        job: QuestionCreationJob,
//        optimizedContent: String,
//        allocations: List<QuestionAllocation>
//    ): List<QuestionDTO>
//}
//
//@Component
//class AllocationQuestionGeneratorImpl(
//    private val gptClient: GptClient,
//    private val parser: com.xapps.question_generator.workflow.questions_json_parser.QuestionsJsonParser,
//    private val selfTestPromptBuilder: PromptBuilder
//) : AllocationQuestionGenerator {
//
//    private val logger = LoggerFactory.getLogger(javaClass)
//    private val semaphore = Semaphore(4)
//
//    override suspend fun generate(
//        job: QuestionCreationJob,
//        optimizedContent: String,
//        allocations: List<QuestionAllocation>
//    ): List<QuestionDTO> = coroutineScope {
//
//        allocations.map { allocation ->
//            async {
//                generateForAllocation(
//                    optimizedContent = optimizedContent,
//                    allocation = allocation
//                )
//            }
//        }
//        .awaitAll()
//        .flatten()
//    }
//
//    private suspend fun generateForAllocation(
//        optimizedContent: String,
//        allocation: QuestionAllocation
//    ): List<QuestionDTO> = semaphore.withPermit {
//        outcomeOf {
//            ensureActive()
//
//            val prompt = selfTestPromptBuilder.build(optimizedContent, allocation)
//
//            val json = gptClient.generateJsonResponse(prompt)
//            ensureActive()
//
//            parser.parse(json, allocation)
//        }.getOrElse { error ->
//            logger.error("Failed to generate questions for allocation $allocation", error)
//            throwGenerationFailedException(
//                "Failed for allocation ${allocation.questionType} at difficulty ${allocation.difficulty}. Error: ${error.message}"
//            )
//        }
//    }
//}
