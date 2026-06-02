package com.xapps.question_generator.job.persistence.entity

import com.xapps.model.NoteSummaryStyleCode
import com.xapps.question_generator.infrastructure.object_store.ObjectKey

data class NoteSummaryGenerationSpecDocument(
    val noteSummaryId: String,
    val userId: String,
    val fileKey: ObjectKey,
    val noteSummaryStyleCode: NoteSummaryStyleCode
)