package com.xapps.question_generator.question_generation_workflow

import com.xapps.messaging.kafka.events.QuestionsGeneratedEvent
import com.xapps.question_generator.infrastructure.messaging.kafka.producer.KafkaSelfTestQuestionsGeneratedEventPublisher
import com.xapps.question_generator.question_generation_workflow.pipeline.QuestionPublisher
import com.xapps.question_generation.dto.QuestionDTO
import com.xapps.question_generation.QuestionGenerationSpec
import org.springframework.stereotype.Component

@Component
class SelfTestQuestionPublisher(
    private val generatedEventPublisher: KafkaSelfTestQuestionsGeneratedEventPublisher,
) : QuestionPublisher {

    override suspend fun publishQuestions(
        questions: List<QuestionDTO>,
        spec: QuestionGenerationSpec,
    ) {
        val event = QuestionsGeneratedEvent(
            userId = spec.userId,
            jobId = spec.jobId,
            quizId = spec.quizId,
            questions = questions
        )

        generatedEventPublisher.publishQuestionsGenerated(event)
    }
}