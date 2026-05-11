//package com.xapps.question_generator.infrastructure.rabbitmq
//
//import org.springframework.amqp.core.Binding
//import org.springframework.amqp.core.BindingBuilder
//import org.springframework.amqp.core.DirectExchange
//import org.springframework.amqp.core.Queue
//import org.springframework.amqp.rabbit.connection.ConnectionFactory
//import org.springframework.amqp.rabbit.core.RabbitTemplate
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//
//// Why: exchange + queue + retry queue + JSON message conversion.
//// The retry queue design here is a simple TTL-based retry
//// (message goes to retry queue with TTL, which dead-letters back to
//// main exchange). For production you can implement more advanced backoff
//// with multiple retry queues or RabbitMQ delayed message plugin.
//
//@Configuration
//class RabbitConfig {
//    companion object {
//        const val EXCHANGE = "generation_job_exchange"
//        const val QUEUE = "generation_job_queue"
//        const val ROUTING_KEY = "generation.job"
//        const val DLX = "generation_job_dlx"
//        const val RETRY_QUEUE = "generation_job_retry_queue"
//    }
//
//    @Bean
//    fun exchange(): DirectExchange = DirectExchange(EXCHANGE, true, false)
//
//    @Bean
//    fun dlxExchange(): DirectExchange = DirectExchange(DLX, true, false)
//
//    @Bean
//    fun queue(): Queue {
//        val args = HashMap<String, Any>()
//        // Retry queue pattern: messages from retry queue go to main queue after TTL
//        return Queue(QUEUE, true, false, false, args)
//    }
//
//    @Bean
//    fun retryQueue(): Queue {
//        val args = HashMap<String, Any>()
//        // messages dead-lettered to main exchange after TTL
//        args["x-dead-letter-exchange"] = EXCHANGE
//        args["x-dead-letter-routing-key"] = ROUTING_KEY
//        args["x-message-ttl"] = 10_000 // 10s default retry delay (adjust as needed)
//        return Queue(RETRY_QUEUE, true, false, false, args)
//    }
//
//    @Bean
//    fun binding(queue: Queue, exchange: DirectExchange): Binding =
//        BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY)
//
//    @Bean
//    fun retryBinding(retryQueue: Queue, dlxExchange: DirectExchange): Binding =
//        BindingBuilder.bind(retryQueue).to(dlxExchange).with(ROUTING_KEY)
//
//    @Bean
//    fun jackson2JsonMessageConverter(): Jackson2JsonMessageConverter = Jackson2JsonMessageConverter()
//
//    @Bean
//    fun rabbitTemplate(connectionFactory: ConnectionFactory, converter: Jackson2JsonMessageConverter): RabbitTemplate {
//        val rt = RabbitTemplate(connectionFactory)
//        rt.messageConverter = converter
//        return rt
//    }
//}