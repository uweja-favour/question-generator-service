package com.xapps.question_generator.workflow.pipeline

import org.springframework.stereotype.Component

@Component
class RetryPolicy(
    val maxAttempts: Int = 2
)