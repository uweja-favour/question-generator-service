package com.xapps.question_generator.infrastructure.object_store

data class PutResult(
    val key: ObjectKey,
    val metadata: ObjectMetadata
)