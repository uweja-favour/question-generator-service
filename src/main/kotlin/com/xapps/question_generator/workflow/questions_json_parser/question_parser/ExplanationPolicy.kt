package com.xapps.question_generator.workflow.questions_json_parser.question_parser

import com.xapps.question_generator.workflow.QuestionSchemaHolder
import com.xapps.model.Difficulty
import com.xapps.question_generator.workflow.questions_json_parser.question_parser.optionalString
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ExplanationPolicy: QuestionSchemaHolder() {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun extract(
        obj: JsonObject,
        difficulty: Difficulty,
    ): String? {
        val expl = obj.optionalString(EXPLANATION)

        when (difficulty) {
            Difficulty.VERY_EASY, Difficulty.EASY ->
                if (!expl.isNullOrBlank())
                    logger.info("Q#$obj: explanation must not exist for $difficulty")
            else ->
                if (expl.isNullOrBlank())
                    logger.info("Q#$obj: explanation required for $difficulty")
        }

        return expl
    }
}
