package com.xapps.question_generator.file_text_extractor.txt

import com.xapps.question_generator.file_text_extractor.ExtractedText
import com.xapps.question_generator.file_text_extractor.TextExtractionException
import com.xapps.question_generator.file_text_extractor.TextExtractor
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class TxtTextExtractor : TextExtractor {
    override fun supports(extension: String): Boolean =
        extension == "txt"

    override suspend fun extract(input: InputStream, fileName: String): ExtractedText =
        input.bufferedReader()
            .use { it.readText() }
            .let { ExtractedText.of(it, fileName) }
}