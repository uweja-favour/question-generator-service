package com.xapps.question_generator.file_text_extractor

import java.io.InputStream

interface TextExtractor {
    fun supports(extension: String): Boolean
    suspend fun extract(input: InputStream, fileName: String): ExtractedText
}
