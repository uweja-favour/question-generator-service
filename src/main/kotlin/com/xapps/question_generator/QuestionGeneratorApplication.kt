package com.xapps.question_generator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [
        JacksonAutoConfiguration::class,
        org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration::class,
    ]
)
class QuestionGeneratorService

fun main(args: Array<String>) {
    runApplication<QuestionGeneratorService>(*args)
}

// RUN WITH DEV PROFILE
// ./gradlew :question-generator-service:bootRun --args='--spring.profiles.active=dev'

// ./gradlew :question-generator-service:bootRun
