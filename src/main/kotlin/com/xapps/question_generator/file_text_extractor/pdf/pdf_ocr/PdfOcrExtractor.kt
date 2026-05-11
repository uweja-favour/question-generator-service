package com.xapps.question_generator.file_text_extractor.pdf.pdf_ocr

import org.apache.pdfbox.rendering.PDFRenderer

interface PdfOcrExtractor {
    suspend fun extract(renderer: PDFRenderer, pageIndex: Int): String?
}
