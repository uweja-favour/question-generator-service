package com.xapps.question_generator.file_text_extractor.ppt

import com.xapps.question_generator.file_text_extractor.ExtractedText
import com.xapps.question_generator.file_text_extractor.TextExtractionException
import com.xapps.question_generator.file_text_extractor.TextExtractor
import org.apache.poi.hslf.usermodel.HSLFSlideShow
import org.apache.poi.hslf.usermodel.HSLFTextShape
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class PptTextExtractor : TextExtractor {

    override fun supports(extension: String): Boolean =
        extension == "ppt"

    override suspend fun extract(input: InputStream, fileName: String): ExtractedText =
        HSLFSlideShow(input).use { ppt ->
            ppt.slides.joinToString("\n") { slide ->
                slide.shapes
                    .filterIsInstance<HSLFTextShape>()
                    .joinToString(" ") { it.text.orEmpty() }
            }
        }.let { ExtractedText.of(it, fileName) }
}