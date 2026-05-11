//package com.xapps.question_generator.infrastructure.redis
//
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.data.redis.connection.RedisConnectionFactory
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
//import org.springframework.data.redis.core.RedisTemplate
//import org.springframework.data.redis.listener.RedisMessageListenerContainer
//
////@Configuration
////class RedisConfig {
////
////    @Bean
////    fun redisClient(): RedisClient =
////        RedisClient.create("redis://localhost:6379")
////
////    @Bean
////    fun redisPubSubConnection(
////        redisClient: RedisClient
////    ): StatefulRedisPubSubConnection<String, String> =
////        redisClient.connectPubSub()
////}
//
//@Configuration
//class RedisConfig {
//
//    @Bean
//    fun redisConnectionFactory(): LettuceConnectionFactory =
//        LettuceConnectionFactory("localhost", 6379)
//
//    @Bean
//    fun redisTemplate(factory: RedisConnectionFactory): RedisTemplate<String, Any> =
//        RedisTemplate<String, Any>().apply {
//            connectionFactory = factory
//        }
//
//    @Bean
//    fun redisMessageListenerContainer(
//        factory: RedisConnectionFactory
//    ): RedisMessageListenerContainer =
//        RedisMessageListenerContainer().apply {
//            setConnectionFactory(factory)
//        }
//}
//
//
//// Get Redis to work
//
//// C:\Users\Favour>C:\redis\redis-cli.exe ping
//// PONG
////
//// C:\Users\Favour>