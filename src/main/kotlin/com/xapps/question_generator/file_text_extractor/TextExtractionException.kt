package com.xapps.question_generator.file_text_extractor

sealed class TextExtractionException(
    open val fileName: String,
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class UnsupportedFileType(
    override val fileName: String,
    val extension: String
) : TextExtractionException(
    fileName,
    "Unsupported file type: .$extension"
)

class EmptyExtractedContent(
    override val fileName: String
) : TextExtractionException(
    fileName,
    "No text could be extracted"
)

class ParserFailure(
    override val fileName: String,
    val format: String,
    cause: Throwable?
) : TextExtractionException(
    fileName,
    "Failed to parse $format file",
    cause
)

class OcrFailure(
    override val fileName: String,
    val engine: String,
    cause: Throwable? = null
) : TextExtractionException(
    fileName,
    "OCR failed using $engine",
    cause
)

class IoFailure(
    override val fileName: String,
    cause: Throwable?
) : TextExtractionException(
    fileName,
    "I/O error while reading file",
    cause
)