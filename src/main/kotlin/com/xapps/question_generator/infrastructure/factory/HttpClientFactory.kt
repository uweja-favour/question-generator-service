package com.xapps.question_generator.infrastructure.factory

import com.xapps.platform.serialization_core.BaseJsonEngine
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.HttpHeaders.ContentEncoding
import io.ktor.serialization.kotlinx.json.*
import io.netty.handler.codec.compression.StandardCompressionOptions.deflate
import io.netty.handler.codec.compression.StandardCompressionOptions.gzip
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

object HttpClientFactory {

    private val log = LoggerFactory.getLogger(HttpClientFactory::class.java)
    private val isShutdown = AtomicBoolean(false)

    @Volatile
    private var apiKey: String? = null  // ← add this

    fun initialize(openAiApiKey: String) {  // ← call this at startup
        this.apiKey = openAiApiKey
    }

    @Volatile
    private var httpClient: HttpClient? = null

    fun getClient(): HttpClient {
        if (isShutdown.get()) {
            throw IllegalStateException("HttpClient has been shut down")
        }

        return httpClient ?: synchronized(this) {
            httpClient ?: createClient().also {
                httpClient = it
                log.info("🌐 HTTP Client initialized with optimized configuration")
            }
        }
    }

    private fun createClient(): HttpClient {
        return HttpClient(CIO) {

            // JSON parsing configuration
            install(ContentNegotiation) {
                json(BaseJsonEngine.json)
            }

            install(ContentEncoding) {
                gzip()
                deflate()
            }

            // Logging configuration
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }

            // Disable automatic retries; handled manually if needed
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 0)
                retryOnException(maxRetries = 0, retryOnTimeout = false)
            }

            // Timeout configuration
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
            }

            // Engine tuning
            engine {
                maxConnectionsCount = 50

                endpoint {
                    maxConnectionsPerRoute = 10
                    pipelineMaxSize = 10
                    keepAliveTime = 30_000_000_000
                    connectTimeout = 15_000_000_000
                    connectAttempts = 2
                }

                requestTimeout = 1_800_000_000_000
            }

            // Response exception logging
            HttpResponseValidator {
                handleResponseExceptionWithRequest { exception, request ->
                    log.warn("Request to ${request.url} failed: ${exception.message}")
                }
            }

            // Default request setup
            defaultRequest {
                // ✅ This is the fix — attach the Bearer token to every request
                apiKey?.let { key ->
                    headers.append("Authorization", "Bearer $key")
                } ?: log.error("⚠️ API key is null — requests will be unauthorized")
            }
        }
    }

    fun shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            synchronized(this) {
                httpClient?.let { client ->
                    try {
                        client.close()
                        log.info("✅ HTTP Client shut down successfully")
                    } catch (e: Exception) {
                        log.warn("⚠️ Error shutting down HTTP Client: ${e.message}")
                    } finally {
                        httpClient = null
                    }
                }
            }
        }
    }

    fun isHealthy(): Boolean = !isShutdown.get() && httpClient != null

    fun getClientInfo(): String = buildString {
        append("HttpClient Status: ")
        append(if (isShutdown.get()) "SHUTDOWN" else "ACTIVE")
        append(", Instance: ")
        append(if (httpClient != null) "AVAILABLE" else "NULL")
    }
}
