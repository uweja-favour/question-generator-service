package com.xapps.question_generator

import com.xapps.platform.core.common.extensions.bytesToKb
import com.xapps.platform.core.common.extensions.bytesToMb
import com.xapps.platform.core.common.extensions.mb
import org.springframework.web.multipart.MultipartFile

/**
 * Validates files and extracted content for AI question generation.
 */
object ContentSizeValidator {

    private val MAX_CONTENT_LENGTH_BYTES = 10.mb     // 10 MB for extracted content
    private const val MIN_CONTENT_LENGTH_CHARS = 100           // minimum characters
    private val MAX_TOTAL_FILE_SIZE_BYTES = 200.mb // 200 MB cumulative

    /**
     * Validates that uploaded files are valid for AI question generation.
     * Throws [ContentSizeException] if validation fails.
     *
     * Requirements:
     * - At least one file uploaded
     * - Total size of all files ≤ 200MB
     * - No file is empty
     */
    fun requireValidFiles(listOfFiles: List<MultipartFile>) {
        if (listOfFiles.isEmpty()) throw ContentSizeException("No files were uploaded")

        val totalSize = listOfFiles.sumOf { it.size }
        if (totalSize > MAX_TOTAL_FILE_SIZE_BYTES) {
            throw ContentSizeException("Total size of uploaded files is too large (> 200MB, actual: ${totalSize.bytesToMb} MB)")
        }
    }

    /**
     * Validates that extracted text content meets length requirements.
     * - Minimum number of characters
     * - Maximum byte size (UTF-8) ≤ 10 MB
     */
    fun requireValidContent(content: String) {
        val byteSize = content.toByteArray(Charsets.UTF_8).size

        if (byteSize > MAX_CONTENT_LENGTH_BYTES) {
            throw ContentSizeException("Extracted content is too large (> 10MB, actual: ${byteSize.bytesToKb} KB)")
        }
        if (content.length < MIN_CONTENT_LENGTH_CHARS) {
            throw ContentSizeException("Extracted content is too small (< $MIN_CONTENT_LENGTH_CHARS characters)")
        }
    }
}


class ContentSizeException(message: String) : RuntimeException(message)
class SpecValidationException(message: String) : RuntimeException(message)