package com.xapps.question_generator.file_text_extractor.pptx

import com.xapps.question_generator.file_text_extractor.ExtractedText
import com.xapps.question_generator.file_text_extractor.TextExtractionException
import com.xapps.question_generator.file_text_extractor.TextExtractor
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFTextShape
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class PptxTextExtractor : TextExtractor {

    override fun supports(extension: String): Boolean =
        extension == "pptx"

    override suspend fun extract(input: InputStream, fileName: String): ExtractedText =
        XMLSlideShow(input).use { ppt ->
            ppt.slides.joinToString("\n") { slide ->
                slide.shapes
                    .filterIsInstance<XSLFTextShape>()
                    .joinToString(" ") { it.text.orEmpty() }
            }
        }.let { ExtractedText.of(it, fileName) }
}