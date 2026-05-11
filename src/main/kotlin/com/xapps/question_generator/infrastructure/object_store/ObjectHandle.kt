package com.xapps.question_generator.infrastructure.object_store

import java.io.InputStream

data class ObjectHandle(
    val metadata: ObjectMetadata,
    val streamProvider: () -> InputStream
)