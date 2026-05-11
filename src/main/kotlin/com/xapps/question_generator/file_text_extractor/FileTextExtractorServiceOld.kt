//package com.xapps.question_generator.file_text_extractor
//
//import com.xapps.platform.core.file.extensionOrNull
//import com.xapps.platform.core.file.withTempFile
//import com.xapps.platform.core.outcome.getOrThrow
//import com.xapps.platform.core.outcome.mapCatching
//import com.xapps.platform.core.outcome.onFailure
//import com.xapps.platform.core.outcome.onSuccess
//import com.xapps.platform.core.outcome.outcomeOf
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.awaitAll
//import kotlinx.coroutines.coroutineScope
//import kotlinx.coroutines.withContext
//import net.sourceforge.tess4j.Tesseract
//import org.apache.poi.hslf.usermodel.HSLFSlideShow
//import org.apache.poi.hslf.usermodel.HSLFTextShape
//import org.apache.poi.hwpf.HWPFDocument
//import org.apache.poi.hwpf.extractor.WordExtractor
//import org.apache.poi.xslf.usermodel.XMLSlideShow
//import org.apache.poi.xslf.usermodel.XSLFTextShape
//import org.apache.poi.xwpf.usermodel.XWPFDocument
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Component
//import org.springframework.web.multipart.MultipartFile
//import software.amazon.awssdk.core.internal.http.pipeline.RequestPipelineBuilder.async
//import java.io.File
//import java.io.IOException
//import java.io.InputStream
//
//@Component
//class FileTextExtractorService(
//    private val pdfContentExtractor: PdfContentExtractor
//) {
//
//    // IMPORTANT: These are native programs, install them on your server (Ubuntu/Debian):
//    // sudo apt update
//    // sudo apt install poppler-utils tesseract-ocr
//
//    private val logger = LoggerFactory.getLogger(javaClass)
//    private val ocrEngine by lazy {
//        Tesseract().apply {
//            val possiblePaths = setOf(
//                "/usr/share/tesseract-ocr/4.00/tessdata",
//                "/usr/share/tesseract-ocr/tessdata",
//                "/usr/local/share/tessdata",
//                "/opt/homebrew/share/tessdata"
//            )
//            val validPath = possiblePaths.firstOrNull { File(it).exists() }
//            if (validPath == null) {
//                logger.warn("⚠️ No valid Tesseract data path found, OCR may fail")
//            } else setDatapath(validPath)
//
//            setLanguage("eng")
//        }
//    }
//
//    suspend fun extractText(files: List<MultipartFile>): String = coroutineScope {
//        require(files.isNotEmpty()) { "No files provided." }
//
//        logger.info("Extracting text from ${files.size} files")
//        files.map { file ->
//            async(Dispatchers.IO) { extractText(file) }
//        }.awaitAll().joinToString("\n\n")
//    }
//
//    suspend fun extractText(file: MultipartFile): String = withContext(Dispatchers.IO) {
//        val name = file.originalFilename.orEmpty().ifBlank { "unknown_file" }
//        val ext = name.substringAfterLast('.', "").lowercase()
//
//        logger.info("🔍 Extracting text from file: $name (type: $ext)")
//
//        // TODO Add support for "epub"
//        val extractor: suspend (InputStream) -> String = when (ext) {
//            "txt" -> ::extractTxt
//            "docx" -> ::extractDocx
//            "pptx" -> ::extractPptx
//            "doc" -> ::extractDoc
//            "ppt" -> ::extractOldPpt
//            "pdf" -> pdfContentExtractor::extractPdfContent
//            "jpg", "jpeg", "png", "bmp", "tiff", "webp" -> { _ -> extractImageText(file) }
//            else -> { _ -> throw FileExtractionException.UnsupportedType(ext) }
//        }
//
//        safeExtract(name) { extractor(file.inputStream) }
//    }
//
//    private fun extractImageText(file: MultipartFile): String = runCatching {
//        withTempFile(".${file.extensionOrNull() ?: "jpg"}") { temp ->
//            file.transferTo(temp)
//            val text = ocrEngine.doOCR(temp)
//            text.takeIf { it.isNotBlank() }
//                ?: throw FileExtractionException.OCRFailure(file.originalFilename ?: "image", "Tesseract", "No text detected")
//        }
//    }.onSuccess {
//        logger.info("✅ OCR extracted ${it.length} chars from image")
//    }.onFailure { e ->
//        logger.error("❌ OCR failed: ${file.originalFilename} - ${e.message}", e)
//    }.getOrElse { throw when (it) {
//        is FileExtractionException -> it
//        else -> FileExtractionException.OCRFailure(file.originalFilename ?: "image", "Tesseract", it.message)
//    } }
//
//    private fun extractTxt(input: InputStream): String =
//        parseWith("TXT") {
//            input.bufferedReader()
//                .use { it.readText() }
//        }
//
//    private fun extractDocx(input: InputStream): String =
//        parseWith("DOCX") {
//            XWPFDocument(input).use { doc ->
//                doc.paragraphs
//                    .joinToString("\n") { it.text }
//            }
//        }
//
//    private fun extractDoc(input: InputStream): String =
//        parseWith("DOC") {
//            HWPFDocument(input).use { doc ->
//                WordExtractor(doc).use { it.text }
//            }
//        }
//
//    private fun extractPptx(input: InputStream): String =
//        parseWith("PPTX") {
//            XMLSlideShow(input).use { ppt ->
//                ppt.slides.joinToString("\n") { slide ->
//                    slide.shapes
//                        .filterIsInstance<XSLFTextShape>()
//                        .joinToString(" ") { it.text.orEmpty() }
//                }
//            }
//        }
//
//    private fun extractOldPpt(input: InputStream): String =
//        parseWith("PPT") {
//            HSLFSlideShow(input).use { ppt ->
//                ppt.slides.joinToString("\n") { slide ->
//                    slide.shapes
//                        .filterIsInstance<HSLFTextShape>()
//                        .joinToString(" ") { it.text.orEmpty() }
//                }
//            }
//        }
//
//    private suspend inline fun safeExtract(
//        fileName: String,
//        crossinline block: suspend () -> String?
//    ): String = outcomeOf { block() }.mapCatching { outcome ->
//        outcome.takeUnless { it.isNullOrBlank() }
//            ?: throw FileExtractionException.EmptyContent(fileName)
//    }.onSuccess {
//        logger.info("✅ Extracted ${it.length} characters from $fileName")
//    }.onFailure { error ->
//        logger.error("❌ Extraction failed for $fileName: ${error.message}", error.exception)
//
//        throw when (error.exception) {
//            is FileExtractionException -> error.exception // already typed
//            is IOException -> FileExtractionException.IOFailure(fileName, error.message ?: "Unknown IO error")
//            else -> FileExtractionException.Unexpected(fileName, error.message)
//        }
//    }.getOrThrow()
//
//    private fun parseWith(format: String, block: () -> String): String =
//        runCatching {
//            block().takeIf { it.isNotBlank() }
//                ?: throw FileExtractionException.EmptyContent("unknown.$format")
//        }.onSuccess {
//            logger.info("✅ Parsed ${it.length} characters from $format file")
//        }.onFailure { e ->
//            logger.error("❌ $format extract failed: ${e.message}", e)
//        }.getOrElse {
//            throw when (it) {
//                is FileExtractionException -> it
//                else -> FileExtractionException.ParserFailure("unknown.$format", format, it.message)
//            }
//        }
//
//    /**
//     * Returns the lowercase file extension (without the dot) of the uploaded file,
//     * or `null` if the filename is missing or has no extension.
//     */
//    fun MultipartFile.extensionOrNull(): String? =
//        originalFilename.extensionOrNull()
//
//}
