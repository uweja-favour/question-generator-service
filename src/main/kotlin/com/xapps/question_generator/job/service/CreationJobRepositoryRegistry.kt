package com.xapps.question_generator.job.service

import com.xapps.question_generator.job.domain.model.CreationJob
import com.xapps.question_generator.job.domain.model.NoteSummaryCreationJob
import com.xapps.question_generator.job.domain.model.QuestionCreationJob
import com.xapps.question_generator.job.domain.repository.NoteSummaryCreationJobRepository
import com.xapps.question_generator.job.domain.repository.QuestionCreationJobRepository
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class CreationJobRepositoryRegistry(
    questionRepository: QuestionCreationJobRepository,
    noteSummaryRepository: NoteSummaryCreationJobRepository
) {

    private val repositories:
        Map<KClass<out CreationJob>, CreationJobRepository<out CreationJob>> =
        mapOf(
            QuestionCreationJob::class to questionRepository,
            NoteSummaryCreationJob::class to noteSummaryRepository
        )

    @Suppress("UNCHECKED_CAST")
    fun <T : CreationJob> repositoryFor(
        type: KClass<out T>
    ): CreationJobRepository<T> {
        return repositories[type] as?
            CreationJobRepository<T>
            ?: error("No repository registered for ${type.simpleName}")
    }

    fun allRepositories(): List<CreationJobRepository<out CreationJob>> =
        repositories.values.toList()
}