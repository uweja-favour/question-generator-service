//package com.xapps.question_generator.gpt_service
//
//import com.xapps.question_generator.infrastructure.factory.HttpClientFactory
//import io.ktor.client.request.*
//import io.ktor.client.statement.*
//import io.ktor.client.plugins.*
//import jakarta.annotation.PreDestroy
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import kotlinx.serialization.json.JsonObject
//import kotlinx.serialization.json.addJsonObject
//import kotlinx.serialization.json.buildJsonObject
//import kotlinx.serialization.json.put
//import kotlinx.serialization.json.putJsonArray
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.stereotype.Service
//import kotlin.math.min
//
///**
// * Public interface for GPT client services.
// */

//
//
///**
// * Concrete GPT client implementation using OpenAI API.
// * Handles HTTP, retries, model selection, and delegates parsing to GptResponseParser.
// */
//@Service
//class GptService_old(
//    @Value("\${openai.api.key}") private val apiKey: String,
//    @Value("\${openai.gpt.max_retries:2}") private val maxRetries: Int,
//    private val gptModelSelector: GptModelSelector
//) : GptClient {
//
//    private val logger = LoggerFactory.getLogger(javaClass)
//    private val httpClient by lazy { HttpClientFactory.getClient() }
//    private val gptRetryPolicy = GptRetryPolicy(maxRetries)
//
//    companion object {
//        private const val BASE_URL = "https://api.openai.com/v1/chat/completions"
//    }
//
//    override suspend fun generateJsonResponse(prompt: String, allowGpt5: Boolean): JsonObject {
//        prompt.makeLog("THE PROMPT COMING IN TO THE GENERATE_JSON_RESPONSE")
//        val raw = sendPrompt(prompt, allowGpt5)
//        return GptResponseParser.extractJsonContent(raw).also { it.makeLog("Json Response") }
//    }
//
//    override suspend fun generateStringResponse(prompt: String, allowGpt5: Boolean): String {
//        prompt.makeLog("THE PROMPT COMING IN TO THE GENERATE_STRING_RESPONSE")
//        val raw = sendPrompt(prompt, allowGpt5)
//        return GptResponseParser.extractStringContent(raw).also { it.makeLog("String Response") }
//    }
//
//    private fun Any.makeLog(label: String) {
//        repeat(5) { println() }
//        logger.info("$label: $this")
//        repeat(5) { logger.info("""    """) }
//    }
//
//    /**
//     * Sends the prompt to GPT and returns raw response text.
//     */
//    private suspend fun sendPrompt(prompt: String, allowGpt5: Boolean): String {
//        val model = gptModelSelector.selectModel(prompt, allowGpt5).also {
//            logger.info("Chosen model: $it")
//        }
//
//        val requestJson = buildRequestJson(model, prompt)
//
//        return gptRetryPolicy.executeWithRetry(
//            action = { attempt -> executeRequest(requestJson, attempt) },
//            onFailure = { attempt, e -> logger.warn("GPT request failed on attempt $attempt: ${e.message}") }
//        )
//    }
//
//    /**
//     * Executes the HTTP request to GPT.
//     */
//    private suspend fun executeRequest(
//        requestJson: JsonObject,
//        attempt: Int
//    ): String = withContext(Dispatchers.IO) {
//        val response: HttpResponse = httpClient.post(BASE_URL) {
//            appendHeaders()
//            setBody(requestJson.toString())
//            appendTimeout(attempt)
//        }
//        response.bodyAsText()
//    }
//
//    private fun HttpRequestBuilder.appendHeaders() =
//        apply {
//            headers {
//                append("Authorization", "Bearer $apiKey")
//                append("Content-Type", "application/json")
//                append("Accept", "application/json")
//                append("Connection", "close")
//                append("Cache-Control", "no-cache")
//            }
//        }
//
//    private fun HttpRequestBuilder.appendTimeout(attempt: Int) =
//        apply {
//            timeout {
//                val baseTimeout = 45_000L
//                val timeoutMillis = min(baseTimeout * (attempt + 1), 180_000L)
//                requestTimeoutMillis = timeoutMillis
//                connectTimeoutMillis = 15_000L
//                socketTimeoutMillis = timeoutMillis
//            }
//        }
//
//    private fun buildRequestJson(model: GptModel, prompt: String) =
//        buildJsonObject {
//            put("model", model.modelName)
//            putJsonArray("messages") {
//                addJsonObject {
//                    put("role", "user")
//                    put("content", prompt)
//                }
//            }
//            put("temperature", 0.1)
//        }
//
//    @PreDestroy
//    fun shutdown() {
//        logger.info("🔄 Shutting down GPT HttpClient...")
//        try {
//            HttpClientFactory.shutdown()
//        } catch (e: Exception) {
//            logger.warn("Error during shutdown: ${e.message}")
//        }
//    }
//}
