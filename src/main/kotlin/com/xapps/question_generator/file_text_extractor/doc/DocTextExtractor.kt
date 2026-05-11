package com.xapps.question_generator.file_text_extractor.doc

import com.xapps.question_generator.file_text_extractor.ExtractedText
import com.xapps.question_generator.file_text_extractor.TextExtractionException
import com.xapps.question_generator.file_text_extractor.TextExtractor
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class DocTextExtractor : TextExtractor {

    override fun supports(extension: String): Boolean =
        extension == "doc"

    override suspend fun extract(input: InputStream, fileName: String): ExtractedText =
        HWPFDocument(input).use { doc ->
            WordExtractor(doc).use { it.text }
        }.let { ExtractedText.of(it, fileName) }
}