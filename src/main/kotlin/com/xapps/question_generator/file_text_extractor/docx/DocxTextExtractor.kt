package com.xapps.question_generator.file_text_extractor.docx

import com.xapps.question_generator.file_text_extractor.ExtractedText
import com.xapps.question_generator.file_text_extractor.TextExtractionException
import com.xapps.question_generator.file_text_extractor.TextExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class DocxTextExtractor : TextExtractor {

    override fun supports(extension: String): Boolean =
        extension == "docx"

    override suspend fun extract(input: InputStream, fileName: String): ExtractedText =
        XWPFDocument(input).use { doc ->
            doc.paragraphs.joinToString("\n") { it.text }
        }.let { ExtractedText.of(it, fileName) }
}
