//package com.xapps.question_generator.workflow.questions_json_parser
//
//import com.xapps.question_generator.workflow.QuestionSchemaHolder
//import com.xapps.question_generator.workflow.questions_json_parser.question_parser.SelfTestQuestionParser
//import com.xapps.questions.contracts.self_test_generation.dto.QuestionDTO
//import com.xapps.questions.contracts.question_generation.QuestionAllocation
//import kotlinx.coroutines.async
//import kotlinx.coroutines.awaitAll
//import kotlinx.coroutines.coroutineScope
//import kotlinx.coroutines.sync.Semaphore
//import kotlinx.coroutines.sync.withPermit
//import kotlinx.serialization.json.*
//import org.springframework.stereotype.Service
//
//@Service
//class QuestionsJsonParser(
//    private val questionParser: SelfTestQuestionParser,
//    private val allocationValidator: QuestionAllocationValidator
//): QuestionSchemaHolder() {
//
//    private val parseSemaphore = Semaphore(15)
//
//    suspend fun parse(
//        questionsJson: JsonObject,
//        allocation: QuestionAllocation
//    ): List<QuestionDTO> = coroutineScope {
//        val questions = questionsJson[QUESTIONS]
//            ?.jsonArray
//            ?: error("Missing or invalid '${QUESTIONS}' array in JSON.")
//
//        val validationJob = async {
//            allocationValidator.validate(questions, allocation)
//        }
//
//        val parsingJob = async {
//            parseQuestions(questions)
//        }
//
//        validationJob.await()
//        parsingJob.await()
//    }
//
//    private suspend fun parseQuestions(
//        questions: JsonArray
//    ): List<QuestionDTO> = coroutineScope {
//        questions.mapIndexed { index, element ->
//            async {
//                parseSingleQuestion(element.jsonObject, index)
//            }
//        }.awaitAll()
//    }
//
//    private suspend fun parseSingleQuestion(
//        obj: JsonObject,
//        index: Int
//    ): QuestionDTO =
//        parseSemaphore.withPermit {
//            val type = requireNotNull(JsonQuestionInspector.detectType(obj)) {
//                "Missing 'questionType' for question at index $index."
//            }
//
//            val difficulty = requireNotNull(JsonQuestionInspector.detectDifficulty(obj)) {
//                "Missing 'difficulty' for question at index $index."
//            }
//
//            return questionParser.parse(type, obj, index, difficulty)
//        }
//}
//
//
