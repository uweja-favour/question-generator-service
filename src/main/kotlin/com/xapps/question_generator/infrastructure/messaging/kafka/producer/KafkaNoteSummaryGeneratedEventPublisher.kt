package com.xapps.question_generator.infrastructure.messaging.kafka.producer

import com.xapps.messaging.kafka.KafkaTopics
import com.xapps.messaging.kafka.events.NoteSummaryGeneratedEvent
import com.xapps.platform.core.compression.ObjectCompressionService
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

interface NoteSummaryGeneratedEventPublisher {
    fun publishNoteSummaryGenerated(event: NoteSummaryGeneratedEvent)
}

@Service
class KafkaNoteSummaryGeneratedEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, ByteArray>,
    private val compressionService: ObjectCompressionService
) : NoteSummaryGeneratedEventPublisher {

    override fun publishNoteSummaryGenerated(event: NoteSummaryGeneratedEvent) {

        val compressed: ByteArray = compressionService.compress(
            NoteSummaryGeneratedEvent.serializer(),
            event
        )

        kafkaTemplate.send(
            KafkaTopics.NoteSummary.GENERATED,
            event.noteSummaryId,
            compressed
        )
    }
}
