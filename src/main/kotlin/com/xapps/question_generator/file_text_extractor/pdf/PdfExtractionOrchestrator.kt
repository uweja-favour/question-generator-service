package com.xapps.question_generator.file_text_extractor.pdf

import com.xapps.question_generator.file_text_extractor.pdf.model.PdfDocumentResult
import com.xapps.question_generator.file_text_extractor.pdf.model.PdfPageResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.internal.http.pipeline.RequestPipelineBuilder.async
import kotlin.time.Duration

@Component
class PdfExtractionOrchestrator(
    private val pageExtractor: PdfPageExtractor
) {

    suspend fun extract(
        doc: PDDocument,
        renderer: PDFRenderer,
        timeout: Duration,
        parallelism: Int
    ): PdfDocumentResult = coroutineScope {

        val semaphore = Semaphore(parallelism)

        val pages = (0 until doc.numberOfPages).map { index ->
            async {
                semaphore.withPermit {
                    withTimeoutOrNull(timeout.inWholeMilliseconds) {
                        pageExtractor.extract(doc, renderer, index)
                    } ?: PdfPageResult(index + 1, null, null)
                }
            }
        }.awaitAll()

        PdfDocumentResult(pages)
    }
}
