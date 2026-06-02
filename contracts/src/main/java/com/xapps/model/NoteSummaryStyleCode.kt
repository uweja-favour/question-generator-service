package com.xapps.model;

import kotlin.jvm.JvmInline;

@JvmInline
value class NoteSummaryStyleCode(val code: String)

enum class NoteSummaryStyle(
    val code: NoteSummaryStyleCode,    
    val displayName: String, 
    val explanation: String
) {
    BULLET_POINTS(
        NoteSummaryStyleCode("bullet_points"),
        "Bullet Points",
        "Presents the main ideas as short, clear bullet points. Best for quick scanning and highlighting key facts."
    ),
    PARAGRAPHS(
        NoteSummaryStyleCode("paragraphs"),
        "Concise Paragraphs",
        "Provides a brief, flowing summary written in full sentences. Ideal for understanding context and connections."
    ),
    SIMPLE_EXPLAIN(
        NoteSummaryStyleCode("simple_explanation"),
        "Simplified Explanation",
        "Breaks down concepts into plain, everyday language. Ideal for beginners or quick understanding."
    );

    companion object {
        val byCode = entries.associateBy { it.code }

        fun fromCode(code: NoteSummaryStyleCode): NoteSummaryStyle? {
            return byCode[code]?.let { return it }
        }
    }
}
