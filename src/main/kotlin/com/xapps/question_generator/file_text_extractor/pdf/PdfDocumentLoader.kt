package com.xapps.question_generator.file_text_extractor.pdf

import com.xapps.question_generator.file_text_extractor.ParserFailure
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class PdfDocumentLoader {

    fun load(input: InputStream, fileName: String): PDDocument =
        runCatching { PDDocument.load(input) }
            .recoverCatching {
                PDDocument.load(input, MemoryUsageSetting.setupMainMemoryOnly())
            }
            .getOrElse {
                throw ParserFailure(fileName, "pdf", it)
            }
}
