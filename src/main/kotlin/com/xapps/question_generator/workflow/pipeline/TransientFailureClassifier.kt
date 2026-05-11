package com.xapps.question_generator.workflow.pipeline

import org.springframework.dao.TransientDataAccessException
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.services.s3.model.S3Exception
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

@Component
class TransientFailureClassifier  {

    fun isTransient(throwable: Throwable?): Boolean {
        return generateSequence(throwable) { it.cause }
            .any { t -> t.isKnownTransient() }
    }

    private fun Throwable.isKnownTransient(): Boolean = when (this) {
        is SocketTimeoutException,
        is ConnectException,
        is UnknownHostException,
        is TimeoutException,
        is ResourceAccessException,
        is SdkClientException,
        is TransientDataAccessException,
        is IOException -> true // network / I/O problems are transient
        is HttpServerErrorException -> true // 5xx server errors

        is S3Exception -> runCatching { statusCode() >= 500 }.getOrDefault(true)

        is RuntimeException -> cause?.let { isTransient(it) } == true

        else -> false
    }
}