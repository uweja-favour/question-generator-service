package com.xapps.question_generator.workflow

import com.xapps.messaging.kafka.events.QuestionsGeneratedEvent
import com.xapps.question_generator.infrastructure.messaging.kafka.producer.KafkaClassroomQuestionsGeneratedEventPublisher
import com.xapps.question_generator.workflow.pipeline.QuestionPublisher
import com.xapps.questions.contracts.self_test_generation.dto.QuestionDTO
import com.xapps.questions.contracts.self_test_generation.model.QuestionGenerationSpec
import org.springframework.stereotype.Component

@Component
class ClassroomQuestionPublisher(
    private val generatedEventPublisher: KafkaClassroomQuestionsGeneratedEventPublisher
) : QuestionPublisher {

    override suspend fun publishQuestions(
        questions: List<QuestionDTO>,
        spec: QuestionGenerationSpec
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