package com.xapps.question_generator.file_text_extractor.pdf.model

import com.xapps.question_generator.file_text_extractor.ExtractedText

data class PdfDocumentResult(
    val pages: List<PdfPageResult>
) {
    fun toExtractedText(fileName: String): ExtractedText =
        ExtractedText.of(
            pages.filterNot { it.isEmpty }
                .joinToString("\n\n") { page ->
                    "PAGE ${page.pageNumber}\n${page.text}"
                },
            fileName
        )
}