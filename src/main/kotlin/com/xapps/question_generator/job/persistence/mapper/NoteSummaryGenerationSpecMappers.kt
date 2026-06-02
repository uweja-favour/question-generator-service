package com.xapps.question_generator.job.persistence.mapper

import com.xapps.model.NoteSummaryStyle
import com.xapps.note_summary.NoteSummaryGenerationSpec
import com.xapps.question_generator.job.persistence.entity.NoteSummaryGenerationSpecDocument

fun NoteSummaryGenerationSpec.toEntity(): NoteSummaryGenerationSpecDocument {
    return NoteSummaryGenerationSpecDocument(
        noteSummaryId = noteSummaryId,
        userId = userId,
        fileKey = fileKey,
        noteSummaryStyleCode = style.code,
    )
}

fun NoteSummaryGenerationSpecDocument.toDomain(): NoteSummaryGenerationSpec {
    return NoteSummaryGenerationSpec(
        noteSummaryId = noteSummaryId,
        userId = userId,
        fileKey = fileKey,
        style = NoteSummaryStyle.fromCode(noteSummaryStyleCode) ?: error("Unknown note-summary style code: $noteSummaryStyleCode")
    )
}