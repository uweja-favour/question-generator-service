package com.xapps.question_generator.infrastructure.messaging.kafka.topics_creation

import com.xapps.messaging.kafka.KafkaTopics
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin
//
//@Configuration
//class KafkaTopicConfig {
//
//    companion object {
//        private const val PARTITIONS = 3
//        private const val REPLICATION_FACTOR: Short = 1.toShort()
//    }
//
//    @Bean
//    fun topics(): List<NewTopic> = listOf(
//        NewTopic(KafkaTopics.Questions.REQUESTED, PARTITIONS, REPLICATION_FACTOR),
//        NewTopic(KafkaTopics.Questions.SELF_TEST_GENERATED, PARTITIONS, REPLICATION_FACTOR),
//        NewTopic(KafkaTopics.Questions.CLASSROOM_GENERATED, PARTITIONS, REPLICATION_FACTOR),
//        NewTopic(KafkaTopics.Quiz.SELF_TEST_PAYLOAD, PARTITIONS, REPLICATION_FACTOR),
//        NewTopic(KafkaTopics.Quiz.CLASSROOM_PAYLOAD, PARTITIONS, REPLICATION_FACTOR),
//        NewTopic(KafkaTopics.User.ONLINE, PARTITIONS, REPLICATION_FACTOR),
//        NewTopic(KafkaTopics.User.OFFLINE, PARTITIONS, REPLICATION_FACTOR),
//    )
//}

// Good baseline:
//
// 3 brokers (containers)
// 3–6 partitions per topic
// replication factor = 2 or 3