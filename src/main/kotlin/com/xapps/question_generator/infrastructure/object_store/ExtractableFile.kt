package com.xapps.question_generator.infrastructure.object_store

import java.io.InputStream

data class ExtractableFile(
    val fileName: String,
    val contentType: String,
    val streamProvider: () -> InputStream
)

fun ObjectHandle.asExtractableFile(): ExtractableFile {

    return ExtractableFile(
        fileName = metadata.originalFilename,
        contentType = metadata.contentType,
        streamProvider = streamProvider
    )
}