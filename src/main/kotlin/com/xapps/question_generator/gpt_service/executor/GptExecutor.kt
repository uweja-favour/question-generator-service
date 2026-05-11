package com.xapps.question_generator.gpt_service.executor

import com.xapps.question_generator.gpt_service.GptRequest

interface GptExecutor {
    suspend fun execute(request: GptRequest): String
}