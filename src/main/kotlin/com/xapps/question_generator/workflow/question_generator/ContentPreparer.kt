package com.xapps.question_generator.workflow.question_generator

import com.xapps.question_generator.file_text_extractor.FileTextExtractorService
import com.xapps.question_generator.infrastructure.object_store.ExtractableFile
import com.xapps.question_generator.workflow.throwInvalidFileException
import org.springframework.stereotype.Service

interface ContentPreparer {
    suspend fun prepare(files: List<ExtractableFile>): String
}

@Service
class ContentPreparerImpl(
    private val fileTextExtractor: FileTextExtractorService
) : ContentPreparer {

    override suspend fun prepare(files: List<ExtractableFile>): String {
        if (files.isEmpty()) {
            throwInvalidFileException("No files provided for question generation.")
        }

//        ContentSizeValidator.requireValidFiles(files)
        val content = fileTextExtractor.extractText(files)
//        ContentSizeValidator.requireValidContent(content)
        return content
    }
}