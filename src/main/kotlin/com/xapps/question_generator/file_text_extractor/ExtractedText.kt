package com.xapps.question_generator.file_text_extractor

@JvmInline
value class ExtractedText private constructor(
    val value: String
) {
    companion object {
        fun of(raw: String, fileName: String): ExtractedText {
            val normalized = raw.trim()
            if (normalized.isEmpty()) {
                throw EmptyExtractedContent(fileName)
            }
            return ExtractedText(normalized)
        }
    }
}
