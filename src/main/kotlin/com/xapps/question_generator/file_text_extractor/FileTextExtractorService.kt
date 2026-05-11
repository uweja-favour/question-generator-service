package com.xapps.question_generator.file_text_extractor

import com.xapps.platform.core.outcome.getOrElse
import com.xapps.platform.core.outcome.onSuccess
import com.xapps.platform.core.outcome.outcomeOf
import com.xapps.question_generator.infrastructure.object_store.ExtractableFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class FileTextExtractorService(
    private val registry: TextExtractorRegistry
) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun extractText(files: List<ExtractableFile>): String =
        supervisorScope {

            require(files.isNotEmpty()) {
                "No files provided."
            }

            files.map { file ->

                async(Dispatchers.IO) {
                    extractText(file).value
                }

            }.awaitAll().joinToString("\n\n")
        }

    suspend fun extractText(
        file: ExtractableFile
    ): ExtractedText = withContext(Dispatchers.IO) {

        log.info("File is: $file")

        log.info("The file name is: ${file.fileName}")

        log.info("Content type is: ${file.contentType}")

        val fileName = file.fileName
        val extension = resolveExtension(file).lowercase()

        val extractor = registry.get(
            extension,
            fileName = fileName
        )

        outcomeOf {

            file.streamProvider().use { stream ->
                extractor.extract(stream, fileName)
            }

        }.onSuccess {

            log.info(
                "Extracted ${it.value.length} chars from $fileName"
            )

        }.getOrElse { error ->

            log.error(
                "An error occurred while extracting text from File: $fileName. The error: ${error.message}",
                error
            )

            if (error is IOException)
                throw IoFailure(fileName, error)
            else
                throw error
        }
    }

    private fun resolveExtension(file: ExtractableFile): String {
        val fileNameExtension = file.fileName
            .substringAfterLast('.', "")
            .lowercase()
            .takeIf { it.isNotBlank() }

        if (fileNameExtension != null) return fileNameExtension

        return mapContentTypeToExtension(file.contentType)
    }

    private fun mapContentTypeToExtension(contentType: String?): String {
        val normalized = contentType
            ?.lowercase()
            ?.substringBefore(';')
            ?.trim()
            ?: return "bin"

        return when (normalized) {

            /* =========================
             * TEXT / MARKUP
             * ========================= */
            "text/plain" -> "txt"
            "text/csv" -> "csv"
            "text/tab-separated-values" -> "tsv"
            "text/html" -> "html"
            "text/css" -> "css"
            "text/javascript" -> "js"
            "application/json" -> "json"
            "application/xml", "text/xml" -> "xml"
            "application/yaml", "text/yaml" -> "yml"

            /* =========================
             * PDF
             * ========================= */
            "application/pdf" -> "pdf"

            /* =========================
             * MICROSOFT OFFICE (LEGACY)
             * ========================= */
            "application/msword" -> "doc"
            "application/vnd.ms-excel" -> "xls"
            "application/vnd.ms-powerpoint" -> "ppt"

            /* =========================
             * MICROSOFT OFFICE (MODERN OOXML)
             * ========================= */
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx"
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "pptx"

            /* =========================
             * IMAGES
             * ========================= */
            "image/jpeg" -> "jpg"
            "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/bmp" -> "bmp"
            "image/webp" -> "webp"
            "image/tiff" -> "tiff"
            "image/svg+xml" -> "svg"
            "image/heic" -> "heic"
            "image/heif" -> "heif"
            "image/avif" -> "avif"
            "image/x-icon" -> "ico"
            "image/vnd.microsoft.icon" -> "ico"

            /* =========================
             * AUDIO
             * ========================= */
            "audio/mpeg" -> "mp3"
            "audio/wav" -> "wav"
            "audio/ogg" -> "ogg"
            "audio/webm" -> "webm"
            "audio/aac" -> "aac"
            "audio/flac" -> "flac"
            "audio/mp4" -> "m4a"

            /* =========================
             * VIDEO
             * ========================= */
            "video/mp4" -> "mp4"
            "video/mpeg" -> "mpeg"
            "video/quicktime" -> "mov"
            "video/x-msvideo" -> "avi"
            "video/webm" -> "webm"
            "video/3gpp" -> "3gp"
            "video/x-matroska" -> "mkv"

            /* =========================
             * ARCHIVES
             * ========================= */
            "application/zip" -> "zip"
            "application/x-zip-compressed" -> "zip"
            "application/x-rar-compressed" -> "rar"
            "application/vnd.rar" -> "rar"
            "application/x-7z-compressed" -> "7z"
            "application/x-tar" -> "tar"
            "application/gzip" -> "gz"
            "application/x-bzip2" -> "bz2"

            /* =========================
             * EXECUTABLE / BINARY
             * ========================= */
            "application/octet-stream" -> "bin"
            "application/x-msdownload" -> "exe"
            "application/x-sh" -> "sh"

            /* =========================
             * FONTS
             * ========================= */
            "font/ttf" -> "ttf"
            "font/otf" -> "otf"
            "font/woff" -> "woff"
            "font/woff2" -> "woff2"
            "application/font-woff" -> "woff"
            "application/font-woff2" -> "woff2"

            /* =========================
             * SCIENTIFIC / DATA
             * ========================= */
            "application/x-hdf" -> "hdf"
            "application/x-netcdf" -> "nc"
            "application/vnd.ms-excel.sheet.macroenabled.12" -> "xlsm"

            /* =========================
             * DEFAULT FALLBACK
             * ========================= */
            else -> when {
                normalized.startsWith("image/") -> "img"
                normalized.startsWith("audio/") -> "audio"
                normalized.startsWith("video/") -> "video"
                normalized.startsWith("text/") -> "txt"
                normalized.startsWith("application/") -> "bin"
                else -> "bin"
            }
        }
    }
}