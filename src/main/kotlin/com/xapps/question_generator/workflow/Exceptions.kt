package com.xapps.question_generator.workflow

import com.xapps.questions.contracts.question_generation.JobId

private class JobNotFoundException(jobId: JobId) : RuntimeException("Job with ID=$jobId was not found")
private class InvalidFileException(msg: String) : RuntimeException(msg)
private class GenerationFailedException(msg: String) : RuntimeException(msg)

public fun throwJobNotFoundException(jobId: JobId): Nothing = throw JobNotFoundException(jobId)
public fun throwInvalidFileException(msg: String): Nothing = throw InvalidFileException(msg)
public fun throwGenerationFailedException(msg: String): Nothing = throw GenerationFailedException(msg)
