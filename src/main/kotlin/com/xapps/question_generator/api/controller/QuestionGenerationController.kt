package com.xapps.question_generator.api.controller

import com.xapps.question_generator.api.service.QuestionsGenerationService
import com.xapps.dto.job.JobDTO
import com.xapps.questions.contracts.question_generation.JobId
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/question_generator")
class QuestionGenerationController(
    private val generationService: QuestionsGenerationService
) {

    @PostMapping(
        "/job",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    suspend fun fetchJob(
        @RequestBody jobId: JobId
    ): JobDTO = generationService.fetchJob(jobId)
}