package com.xapps.question_generator.note_summary_workflow.prompt_builder

import org.springframework.stereotype.Component

@Component
class SummaryPromptTemplate {

    fun build(note: String, instructions: String): String {
        return """
            You are a precise and structured note summarization system.

            Task:
            Summarize the provided note strictly according to the given instructions.

            Instructions:
            $instructions

            Input Note:
            
            $note

            Output Requirements:
            - Output only the final summarized note
            - Do not add commentary
            - Do not explain your process
        """.trimIndent()
    }
}