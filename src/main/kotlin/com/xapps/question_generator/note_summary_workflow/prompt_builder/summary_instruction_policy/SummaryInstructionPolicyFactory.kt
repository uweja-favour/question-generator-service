package com.xapps.question_generator.note_summary_workflow.prompt_builder.summary_instruction_policy

import com.xapps.model.NoteSummaryStyle
import org.springframework.stereotype.Component

@Component
class SummaryInstructionPolicyFactory {

    fun create(style: NoteSummaryStyle): SummaryInstructionPolicy {
        return when (style) {
            NoteSummaryStyle.BULLET_POINTS -> BulletPointsPolicy()
            NoteSummaryStyle.PARAGRAPHS -> ParagraphPolicy()
            NoteSummaryStyle.SIMPLE_EXPLAIN -> SimpleExplanationPolicy()
        }
    }
}