package com.xapps.question_generator.file_text_extractor.pdf.pdf_ocr

import net.sourceforge.tess4j.Tesseract
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.springframework.stereotype.Component
import java.io.File

@Component
class TesseractPdfOcrExtractor : PdfOcrExtractor {

    private val tesseract: Tesseract? = create()

    override suspend fun extract(
        renderer: PDFRenderer,
        pageIndex: Int
    ): String? {
        val engine = tesseract ?: return null

        return runCatching {
            val image = renderer.renderImageWithDPI(pageIndex, 300f, ImageType.GRAY)
            engine.doOCR(image).trim()
        }.getOrNull()
    }

    private fun create(): Tesseract? =
        runCatching {
            val t = Tesseract()
            val path = listOf(
                "/usr/share/tesseract-ocr/5.00/tessdata",
                "/usr/share/tesseract-ocr/4.00/tessdata",
                "/usr/share/tesseract-ocr/tessdata",
                "/opt/homebrew/share/tessdata"
            ).firstOrNull { File(it, "eng.traineddata").exists() }
                ?: return null

            t.setDatapath(path)
            t.setLanguage("eng")
            t
        }.getOrNull()
}

