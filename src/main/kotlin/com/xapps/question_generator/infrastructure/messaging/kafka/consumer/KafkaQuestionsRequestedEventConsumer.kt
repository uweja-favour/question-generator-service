package com.xapps.question_generator.infrastructure.messaging.kafka.consumer

import com.xapps.messaging.kafka.KafkaGroupIds
import com.xapps.messaging.kafka.KafkaTopics
import com.xapps.messaging.kafka.events.QuestionsRequestedEvent
import com.xapps.platform.core.compression.ObjectCompressionService
import com.xapps.question_generator.api.service.QuestionsGenerationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaQuestionsRequestedEventConsumer(
    private val generationService: QuestionsGenerationService,
    private val compressionService: ObjectCompressionService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = [KafkaTopics.Questions.REQUESTED],
        groupId = KafkaGroupIds.QUESTIONS_GENERATOR_SERVICE_GROUP
    )
    fun handleQuestionsRequest(
        payload: ByteArray // QuestionsRequestedEvent
    ) {
        log.info("QuestionsRequestedEvent CONSUMED BY QUESTION-GENERATOR!")

        val event = compressionService.decompress(
            QuestionsRequestedEvent.serializer(),
            payload
        )

        CoroutineScope(Dispatchers.Default).launch {
            generationService.generateQuestions(
                userId = event.userId,
                quizId = event.quizId,
                jobId = event.jobId,
                questionCount = event.questionCount,
                fileKeys = event.fileKeys,
                allocations = event.allocations,
                quizType = event.quizType
            )
        }
    }
}