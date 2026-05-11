package com.xapps.question_generator.file_text_extractor.pdf.model

data class PdfPageResult(
    val pageNumber: Int,
    val textLayer: String?,
    val ocrText: String?
) {
    val text: String =
        listOfNotNull(textLayer, ocrText)
            .joinToString("\n")
            .trim()

    val isEmpty: Boolean get() = text.isBlank()
}
