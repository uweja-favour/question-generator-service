package com.xapps.question_generator.file_text_extractor.pdf

import com.xapps.question_generator.file_text_extractor.pdf.model.PdfPageResult
import com.xapps.question_generator.file_text_extractor.pdf.pdf_ocr.PdfOcrExtractor
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.springframework.stereotype.Component

@Component
class PdfPageExtractor(
    private val textLayerExtractor: PdfTextLayerExtractor,
    private val imageDetector: PdfImageDetector,
    private val ocrExtractor: PdfOcrExtractor?
) {

    suspend fun extract(
        doc: PDDocument,
        renderer: PDFRenderer,
        pageIndex: Int
    ): PdfPageResult {

        val pageNumber = pageIndex + 1

        val textLayer = textLayerExtractor.extract(doc, pageIndex)
            ?.takeIf { it.length >= 10 }

        val ocrText =
            if (textLayer == null && imageDetector.hasImages(doc, pageIndex))
                ocrExtractor?.extract(renderer, pageIndex)
            else null

        return PdfPageResult(
            pageNumber = pageNumber,
            textLayer = textLayer,
            ocrText = ocrText
        )
    }
}

