package com.xapps.question_generator.note_summary_workflow

import com.xapps.messaging.kafka.events.NoteSummaryGeneratedEvent
import com.xapps.note_summary.NoteSummaryGenerationSpec
import com.xapps.question_generator.infrastructure.messaging.kafka.producer.KafkaNoteSummaryGeneratedEventPublisher
import org.springframework.stereotype.Component

interface NoteSummaryPublisher {
    suspend fun publishNoteSummary(
        content: String,
        spec: NoteSummaryGenerationSpec
    )
}

@Component
class NoteSummaryPublisherImpl(
    private val generatedEventPublisher: KafkaNoteSummaryGeneratedEventPublisher,
) : NoteSummaryPublisher {

    override suspend fun publishNoteSummary(
        content: String,
        spec: NoteSummaryGenerationSpec
    ) {
        val event = NoteSummaryGeneratedEvent(
            noteSummaryId = spec.noteSummaryId,
            userId = spec.userId,
            content = content
        )

        generatedEventPublisher.publishNoteSummaryGenerated(event)
    }
}