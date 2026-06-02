//package com.xapps.question_generator.workflow
//
//import com.xapps.question_generator.infrastructure.rabbitmq.RabbitConfig
//import com.xapps.questions.contracts.question_generation.JobId
//import kotlinx.coroutines.*
//import org.springframework.amqp.rabbit.annotation.RabbitListener
//import org.springframework.stereotype.Component
//
//@Component
//class GenerationWorker(
//    private val jobProcessor: JobProcessor
//) {
//
//    @RabbitListener(queues = [RabbitConfig.QUEUE])
//    fun handle(jobIdValue: String) {
//
//        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
//            jobProcessor.run(JobId.of(jobIdValue))
//        }
//    }
//}
