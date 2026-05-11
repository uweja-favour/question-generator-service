package com.xapps.question_generator.file_text_extractor

import org.springframework.stereotype.Component

@Component
class TextExtractorRegistry(
    extractors: List<TextExtractor>
) {
    private val extractorMap =
        extractors.flatMap { extractor ->
            (0..0).map { extractor } // forces Spring instantiation
        }

    fun get(extension: String, fileName: String): TextExtractor =
        extractorMap.firstOrNull { it.supports(extension) }
            ?: throw UnsupportedFileType(fileName = fileName, extension)
}