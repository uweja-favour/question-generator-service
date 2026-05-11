package com.xapps.question_generator.infrastructure.messaging.kafka.topics_creation

import com.xapps.messaging.kafka.KafkaTopics
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.errors.TopicExistsException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
class KafkaTopicProvisioner(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String,

    @Value("\${spring.kafka.properties.sasl.jaas.config}")
    private val jaasConfig: String
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(KafkaTopicProvisioner::class.java)

    private val topics = listOf(
        KafkaTopics.Questions.REQUESTED,
        KafkaTopics.Questions.SELF_TEST_GENERATED,
        KafkaTopics.Questions.CLASSROOM_GENERATED,

        KafkaTopics.Quiz.SELF_TEST_PAYLOAD,
        KafkaTopics.Quiz.CLASSROOM_PAYLOAD,
        KafkaTopics.Quiz.CLASSROOM_STATE_CHANGED,

        KafkaTopics.NoteSummary.NOTE_SUMMARY_PAYLOAD,

        KafkaTopics.User.ONLINE,
        KafkaTopics.User.OFFLINE
    )

    override fun run(args: ApplicationArguments) {
        log.info("Starting Kafka topic provisioning...")

        val props = mapOf(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,

            "security.protocol" to "SASL_SSL",

            "sasl.mechanism" to "PLAIN",

            "sasl.jaas.config" to jaasConfig
        )

        AdminClient.create(props).use { admin ->
            val existing = admin.listTopics().names().get(10, TimeUnit.SECONDS)

            val toCreate = topics.filterNot { existing.contains(it) }

            log.info("Existing topics: {}", existing)
            log.info("Topics to create: {}", toCreate)

            if (toCreate.isEmpty()) {
                log.info("All topics already exist. Nothing to create.")
                return
            }

            val newTopics = toCreate.map {
                NewTopic(it, 3, 3.toShort())
            }

            try {
                val result = admin.createTopics(newTopics)
                result.all().get(30, TimeUnit.SECONDS)

                log.info("Successfully created all missing topics")

            } catch (ex: Exception) {
                log.error("Topic creation failed", ex)
                throw IllegalStateException("Kafka topic provisioning failed", ex)
            }
        }
    }
}