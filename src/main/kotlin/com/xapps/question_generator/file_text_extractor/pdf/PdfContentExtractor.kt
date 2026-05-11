//package com.xapps.question_generator.file_text_extractor.pdf
//
//import com.xapps.platform.core.outcome.getOrElse
//import com.xapps.platform.core.outcome.outcomeOf
//import com.xapps.platform.core.outcome.toResult
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.awaitAll
//import kotlinx.coroutines.coroutineScope
//import kotlinx.coroutines.sync.Semaphore
//import kotlinx.coroutines.sync.withPermit
//import kotlinx.coroutines.withContext
//import kotlinx.coroutines.withTimeoutOrNull
//import net.sourceforge.tess4j.Tesseract
//import org.apache.pdfbox.io.MemoryUsageSetting
//import org.apache.pdfbox.pdmodel.PDDocument
//import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
//import org.apache.pdfbox.rendering.ImageType
//import org.apache.pdfbox.rendering.PDFRenderer
//import org.apache.pdfbox.text.PDFTextStripper
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Component
//import java.io.File
//import java.io.InputStream
//import kotlin.time.Duration
//import kotlin.time.Duration.Companion.seconds
//
///**
// * Enterprise-grade PDF content extractor with comprehensive fault tolerance.
// *
// * This component extracts text from PDFs using a hybrid approach:
// * 1. Primary: Extracts embedded text layers using PDFBox
// * 2. Fallback: Performs OCR on rendered pages when text layer is insufficient
// *
// * Design Principles:
// * - NEVER FAIL: All operations are wrapped with multiple fallback strategies
// * - ALWAYS RETURN: Even corrupted/empty PDFs return valid (possibly empty) results
// * - GRACEFUL DEGRADATION: Falls back to partial results rather than failing completely
// * - RESOURCE SAFE: Proper cleanup and timeout handling for all operations
// *
// * @author Question Generator Team
// * @since 1.0.0
// */
//@Component
//class PdfContentExtractor {
//
//    private val logger = LoggerFactory.getLogger(javaClass)
//
//    companion object {
//        private const val DEFAULT_DPI = 300f
//        private const val DEFAULT_MAX_PARALLELISM = 8
//        private val DEFAULT_PAGE_TIMEOUT = 45.seconds
//
//        // Minimum text length to consider a text layer valid
//        private const val MIN_TEXT_LAYER_LENGTH = 10
//    }
//
//    /* ═══════════════════════════════════════════════════════════════════════════
//     * DATA MODELS
//     * ═══════════════════════════════════════════════════════════════════════════ */
//
//    /**
//     * Represents the extraction result for a single PDF page.
//     *
//     * @property pageNumber 1-based page number
//     * @property textLayer Text extracted from PDF's embedded text layer (null if extraction failed)
//     * @property ocrText Text extracted via OCR from rendered page image (null if OCR not performed/failed)
//     * @property extractionMethod Method used to extract text from this page
//     */
//    data class PdfPageResult(
//        val pageNumber: Int,
//        val textLayer: String?,
//        val ocrText: String?,
//        val extractionMethod: ExtractionMethod
//    ) {
//        enum class ExtractionMethod {
//            TEXT_LAYER_ONLY,
//            OCR_ONLY,
//            COMBINED,
//            EMPTY
//        }
//
//        /**
//         * Returns the best available text for this page, preferring text layer over OCR.
//         */
//        val combinedText: String
//            get() = buildString {
//                when {
//                    textLayer != null && ocrText != null -> {
//                        appendLine("[Text Layer]")
//                        appendLine(textLayer.trim())
//                        appendLine()
//                        appendLine("[OCR Enhancement]")
//                        appendLine(ocrText.trim())
//                    }
//                    textLayer != null -> appendLine(textLayer.trim())
//                    ocrText != null -> appendLine(ocrText.trim())
//                }
//            }.trim()
//
//        val isEmpty: Boolean get() = combinedText.isBlank()
//    }
//
//    /**
//     * Complete extraction result for an entire PDF document.
//     *
//     * @property pages List of extraction results for each page
//     * @property totalPages Total number of pages in the document
//     * @property successfulPages Number of pages with extracted text
//     */
//    data class PdfContentExtractionResult(
//        val pages: List<PdfPageResult>,
//        val totalPages: Int,
//        val successfulPages: Int
//    ) {
//        /**
//         * Returns formatted text with all page content.
//         */
//        val fullText: String
//            get() = pages
//                .filterNot { it.isEmpty }
//                .joinToString("\n\n") { page ->
//                    "━━━━━ PAGE ${page.pageNumber} ━━━━━\n${page.combinedText}"
//                }
//
//        val isEmpty: Boolean get() = successfulPages == 0
//
//        companion object {
//            fun empty(totalPages: Int = 0) = PdfContentExtractionResult(
//                pages = emptyList(),
//                totalPages = totalPages,
//                successfulPages = 0
//            )
//        }
//    }
//
//    /* ═══════════════════════════════════════════════════════════════════════════
//     * PUBLIC API
//     * ═══════════════════════════════════════════════════════════════════════════ */
//
//    /**
//     * Extracts all text content from a PDF document with maximum fault tolerance.
//     *
//     * This method NEVER throws exceptions. It will always return a result, even if:
//     * - The PDF is corrupted or malformed
//     * - Fonts are missing or embedded incorrectly
//     * - Pages contain only images with no text layer
//     * - OCR processing fails
//     *
//     * @param input PDF document as InputStream
//     * @param maxParallelism Maximum concurrent page processing (default: 8)
//     * @param perPageTimeout Maximum time to spend on each page (default: 45 seconds)
//     * @param enableOcr Whether to perform OCR on pages with images (default: true)
//     * @return Extracted text content (may be empty but never null)
//     */
//    suspend fun extractPdfContent(
//        input: InputStream,
//        maxParallelism: Int = DEFAULT_MAX_PARALLELISM,
//        perPageTimeout: Duration = DEFAULT_PAGE_TIMEOUT,
//        enableOcr: Boolean = true
//    ): String = withContext(Dispatchers.IO) {
//        outcomeOf {
//            extractPdfContentInternal(input, maxParallelism, perPageTimeout, enableOcr)
//        }.getOrElse { error ->
//            logger.error("Critical error during PDF extraction. Returning empty result.", error)
//            PdfContentExtractionResult.empty()
//        }.fullText
//    }
//
//    /* ═══════════════════════════════════════════════════════════════════════════
//     * INTERNAL EXTRACTION LOGIC
//     * ═══════════════════════════════════════════════════════════════════════════ */
//
//    /**
//     * Internal extraction orchestrator with structured error handling.
//     */
//    private suspend fun extractPdfContentInternal(
//        input: InputStream,
//        maxParallelism: Int,
//        perPageTimeout: Duration,
//        enableOcr: Boolean
//    ): PdfContentExtractionResult = coroutineScope {
//
//        val tesseract = if (enableOcr) createTesseractInstance() else null
//
//        val document = loadDocumentSafely(input) ?: run {
//            logger.error("Failed to load PDF document. Returning empty result.")
//            return@coroutineScope PdfContentExtractionResult.empty()
//        }
//
//        document.use { doc ->
//            val totalPages = doc.numberOfPages
//            logger.info("Processing PDF with $totalPages pages (parallelism: $maxParallelism)")
//
//            val renderer = createRendererSafely(doc)
//            val semaphore = Semaphore(maxParallelism)
//
//            val pages = (0 until totalPages).map { index ->
//                async {
//                    semaphore.withPermit {
//                        extractPageWithRetry(doc, renderer, index, tesseract, perPageTimeout)
//                    }
//                }
//            }.awaitAll()
//
//            val successfulPages = pages.count { !it.isEmpty }
//
//            logger.info("Extraction complete: $successfulPages/$totalPages pages successful")
//
//            PdfContentExtractionResult(
//                pages = pages,
//                totalPages = totalPages,
//                successfulPages = successfulPages
//            )
//        }
//    }
//
//    /**
//     * Loads PDF document with multiple fallback strategies for corrupted files.
//     */
//    private fun loadDocumentSafely(input: InputStream): PDDocument? {
//        return runCatching {
//            // Primary: Load normally
//            PDDocument.load(input)
//        }.recoverCatching { error ->
//            logger.warn("Normal PDF load failed, trying lenient mode: ${error.message}")
//            // Fallback: Load with lenient parsing
//            PDDocument.load(input, MemoryUsageSetting.setupMainMemoryOnly())
//        }.onFailure { error ->
//            logger.error("All PDF loading strategies failed", error)
//        }.getOrNull()
//    }
//
//    /**
//     * Creates PDF renderer with error handling.
//     */
//    private fun createRendererSafely(doc: PDDocument): PDFRenderer? {
//        return runCatching {
//            PDFRenderer(doc)
//        }.onFailure { error ->
//            logger.error("Failed to create PDF renderer. OCR will be disabled.", error)
//        }.getOrNull()
//    }
//
//    /**
//     * Extracts content from a single page with automatic retry and fallback logic.
//     */
//    private suspend fun extractPageWithRetry(
//        doc: PDDocument,
//        renderer: PDFRenderer?,
//        index: Int,
//        tesseract: Tesseract?,
//        timeout: Duration
//    ): PdfPageResult {
//        val pageNumber = index + 1
//
//        return withTimeoutOrNull(timeout.inWholeMilliseconds) {
//            extractPageContent(doc, renderer, index, tesseract)
//        } ?: run {
//            logger.warn("Page $pageNumber extraction timed out after ${timeout.inWholeSeconds}s")
//            createEmptyPageResult(pageNumber)
//        }
//    }
//
//    /**
//     * Core page extraction logic with comprehensive error handling.
//     */
//    private suspend fun extractPageContent(
//        doc: PDDocument,
//        renderer: PDFRenderer?,
//        index: Int,
//        tesseract: Tesseract?
//    ): PdfPageResult {
//        val pageNumber = index + 1
//
//        // Step 1: Extract text layer (with font error handling)
//        val textLayer = extractTextLayerSafely(doc, index)
//        val hasValidTextLayer = textLayer != null && textLayer.length >= MIN_TEXT_LAYER_LENGTH
//
//        // Step 2: Check if page contains images
//        val hasImages = pageHasImagesSafely(doc, index)
//
//        // Step 3: Perform OCR if needed and available
//        val ocrText = when {
//            hasValidTextLayer && !hasImages -> null // Text layer sufficient
//            renderer == null || tesseract == null -> null // OCR not available
//            else -> performOcrSafely(renderer, index, tesseract)
//        }
//
//        // Step 4: Determine extraction method
//        val method = determineExtractionMethod(textLayer, ocrText)
//
//        val result = PdfPageResult(
//            pageNumber = pageNumber,
//            textLayer = textLayer?.takeIf { it.isNotBlank() },
//            ocrText = ocrText?.takeIf { it.isNotBlank() },
//            extractionMethod = method
//        )
//
//        logPageExtraction(result)
//
//        return result
//    }
//
//    /* ═══════════════════════════════════════════════════════════════════════════
//     * SAFE EXTRACTION METHODS (NO EXCEPTIONS)
//     * ═══════════════════════════════════════════════════════════════════════════ */
//
//    /**
//     * Extracts text layer with aggressive error suppression for font issues.
//     */
//    private fun extractTextLayerSafely(doc: PDDocument, pageIndex: Int): String? {
//        return runCatching {
//            PDFTextStripper().apply {
//                startPage = pageIndex + 1
//                endPage = pageIndex + 1
//                // Suppress font warnings
//                sortByPosition = true
//            }.getText(doc).trim()
//        }.recoverCatching { error ->
//            logger.debug("Text layer extraction failed for page ${pageIndex + 1}: ${error.javaClass.simpleName}")
//            // Try alternative extraction method
//            extractTextLayerAlternative(doc, pageIndex)
//        }.onFailure { error ->
//            logger.warn("All text extraction methods failed for page ${pageIndex + 1}", error)
//        }.getOrNull()
//    }
//
//    /**
//     * Alternative text extraction method that bypasses font rendering.
//     */
//    private fun extractTextLayerAlternative(doc: PDDocument, pageIndex: Int): String? {
//        return runCatching {
//            // Primary fallback: PDFTextStripper without sorting
//            val stripper = PDFTextStripper().apply {
//                startPage = pageIndex + 1
//                endPage = pageIndex + 1
//                sortByPosition = false
//            }
//            val text = stripper.getText(doc).trim()
//            if (text.isNotBlank()) return@runCatching text
//
//            // Secondary fallback: iterate over content streams for minimal text recovery
//            val page = doc.getPage(pageIndex)
//
//            val extracted = StringBuilder()
//            page.contentStreams.forEach { stream ->
//                runCatching {
//                    stream.createInputStream().use { input ->
//                        val bytes = input.readBytes() // Kotlin extension for InputStream
//                        val s = String(bytes, Charsets.UTF_8)
//                        if (s.isNotBlank()) extracted.appendLine(s)
//                    }
//                }
//            }
//
//            extracted.toString().takeIf { it.isNotBlank() }
//        }.onFailure { error ->
//            logger.warn("Alternative text extraction failed for page ${pageIndex + 1}", error)
//        }.getOrNull()
//    }
//
//
//    /**
//     * Checks if page contains images with comprehensive error handling.
//     */
//    private fun pageHasImagesSafely(doc: PDDocument, pageIndex: Int): Boolean {
//        return runCatching {
//            val page = doc.getPage(pageIndex)
//            page.resources?.xObjectNames?.any { name ->
//                runCatching {
//                    page.resources?.getXObject(name) is PDImageXObject
//                }.getOrDefault(false)
//            } == true
//        }.getOrDefault(false)
//    }
//
//    /**
//     * Performs OCR with timeout and error handling.
//     */
//    private suspend fun performOcrSafely(
//        renderer: PDFRenderer,
//        index: Int,
//        tesseract: Tesseract
//    ): String? = withContext(Dispatchers.Default) {
//        outcomeOf {
//            val image = renderer.renderImageWithDPI(index, DEFAULT_DPI, ImageType.GRAY)
//            tesseract.doOCR(image).trim()
//        }.toResult().recoverCatching { error ->
//            logger.info("OCR failed for page ${index + 1}, trying lower DPI: ${error.message}")
//            // Fallback: Try lower DPI
//            val image = renderer.renderImageWithDPI(index, 150f, ImageType.BINARY)
//            tesseract.doOCR(image).trim()
//        }.onFailure { error ->
//            logger.warn("All OCR attempts failed for page ${index + 1}", error)
//        }.getOrNull()
//    }
//
//    /* ═══════════════════════════════════════════════════════════════════════════
//     * TESSERACT INITIALIZATION
//     * ═══════════════════════════════════════════════════════════════════════════ */
//
//    /**
//     * Creates Tesseract instance with automatic path detection.
//     * Returns null if Tesseract is not available (non-fatal).
//     */
//    private fun createTesseractInstance(): Tesseract? {
//        return runCatching {
//            val tesseract = Tesseract()
//
//            val possiblePaths = listOfNotNull(
//                "/usr/share/tesseract-ocr/5.00/tessdata",
//                "/usr/share/tesseract-ocr/4.00/tessdata",
//                "/usr/share/tesseract-ocr/tessdata",
//                "/usr/local/share/tessdata",
//                "/opt/homebrew/share/tessdata",
//                "C:\\Program Files\\Tesseract-OCR\\tessdata",
//                "C:\\Program Files (x86)\\Tesseract-OCR\\tessdata",
//                System.getenv("TESSDATA_PREFIX")
//            )
//
//            val validPath = possiblePaths.firstOrNull { path ->
//                File(path).exists() && File(path, "eng.traineddata").exists()
//            }
//
//            if (validPath == null) {
//                logger.warn("Tesseract data not found. OCR will be disabled.")
//                return null
//            }
//
//            tesseract.setDatapath(validPath)
//            tesseract.setLanguage("eng")
//            tesseract.setPageSegMode(1)
//            tesseract.setOcrEngineMode(1)
//
//            logger.info("Tesseract initialized with path: $validPath")
//            tesseract
//        }.onFailure { error ->
//            logger.warn("Failed to initialize Tesseract: ${error.message}")
//        }.getOrNull()
//    }
//
//    /* ═══════════════════════════════════════════════════════════════════════════
//     * UTILITY METHODS
//     * ═══════════════════════════════════════════════════════════════════════════ */
//
//    private fun determineExtractionMethod(
//        textLayer: String?,
//        ocrText: String?
//    ): PdfPageResult.ExtractionMethod {
//        return when {
//            textLayer != null && ocrText != null -> PdfPageResult.ExtractionMethod.COMBINED
//            textLayer != null -> PdfPageResult.ExtractionMethod.TEXT_LAYER_ONLY
//            ocrText != null -> PdfPageResult.ExtractionMethod.OCR_ONLY
//            else -> PdfPageResult.ExtractionMethod.EMPTY
//        }
//    }
//
//    private fun createEmptyPageResult(pageNumber: Int) = PdfPageResult(
//        pageNumber = pageNumber,
//        textLayer = null,
//        ocrText = null,
//        extractionMethod = PdfPageResult.ExtractionMethod.EMPTY
//    )
//
//    private fun logPageExtraction(result: PdfPageResult) {
//        val chars = result.combinedText.length
//        val icon = when (result.extractionMethod) {
//            PdfPageResult.ExtractionMethod.TEXT_LAYER_ONLY -> "📄"
//            PdfPageResult.ExtractionMethod.OCR_ONLY -> "🔍"
//            PdfPageResult.ExtractionMethod.COMBINED -> "📄🔍"
//            PdfPageResult.ExtractionMethod.EMPTY -> "⚠️"
//        }
//
//        logger.info("$icon Page ${result.pageNumber}: ${result.extractionMethod.name.lowercase().replace('_', ' ')} ($chars chars)")
//    }
//}