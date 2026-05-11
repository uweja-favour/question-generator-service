package com.xapps.question_generator.file_text_extractor.pdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.stereotype.Component

@Component
class PdfImageDetector {

    fun hasImages(doc: PDDocument, pageIndex: Int): Boolean =
        runCatching {
            val page = doc.getPage(pageIndex)
            page.resources?.xObjectNames?.any {
                page.resources?.getXObject(it) is PDImageXObject
            } == true
        }.getOrDefault(false)
}
