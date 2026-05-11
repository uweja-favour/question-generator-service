package com.xapps.question_generator.file_text_extractor.pdf

import com.xapps.question_generator.file_text_extractor.ExtractedText
import com.xapps.question_generator.file_text_extractor.TextExtractor
import org.apache.pdfbox.rendering.PDFRenderer
import org.springframework.stereotype.Component
import java.io.InputStream
import kotlin.time.Duration.Companion.seconds

@Component
class PdfTextExtractor(
    private val loader: PdfDocumentLoader,
    private val orchestrator: PdfExtractionOrchestrator
) : TextExtractor {

    override fun supports(extension: String): Boolean =
        extension == "pdf"

    override suspend fun extract(
        input: InputStream,
        fileName: String
    ): ExtractedText {

        val document = loader.load(input, fileName)

        document.use { doc ->
            val renderer = PDFRenderer(doc)

            val result = orchestrator.extract(
                doc = doc,
                renderer = renderer,
                timeout = 45.seconds,
                parallelism = 8
            )

            return result.toExtractedText(fileName)
        }
    }
}
