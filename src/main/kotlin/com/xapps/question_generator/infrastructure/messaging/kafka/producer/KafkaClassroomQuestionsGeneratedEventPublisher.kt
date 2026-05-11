package com.xapps.question_generator.infrastructure.messaging.kafka.producer

import com.xapps.messaging.kafka.KafkaTopics
import com.xapps.messaging.kafka.events.QuestionsGeneratedEvent
import com.xapps.platform.core.compression.ObjectCompressionService
import kotlinx.serialization.json.Json
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaClassroomQuestionsGeneratedEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, ByteArray>,
    private val compressionService: ObjectCompressionService
) : QuestionsGeneratedEventPublisher {

    override fun publishQuestionsGenerated(event: QuestionsGeneratedEvent) {

        val compressed: ByteArray = compressionService.compress(
            QuestionsGeneratedEvent.serializer(),
            event
        )

        kafkaTemplate.send(
            KafkaTopics.Questions.CLASSROOM_GENERATED,
            event.jobId.value,
            compressed
        )
    }
}