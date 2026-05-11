package com.xapps.question_generator.file_text_extractor.pdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.stereotype.Component

@Component
class PdfTextLayerExtractor {

    fun extract(doc: PDDocument, pageIndex: Int): String? =
        runCatching {
            PDFTextStripper().apply {
                startPage = pageIndex + 1
                endPage = pageIndex + 1
                sortByPosition = true
            }.getText(doc).trim()
        }.getOrNull()
}
