package com.xapps.question_generator.note_summary_workflow.prompt_builder.summary_instruction_policy

interface SummaryInstructionPolicy {
    fun instructions(): String
}