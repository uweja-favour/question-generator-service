package com.xapps.question_generator.infrastructure.serialization

import com.xapps.platform.serialization_core.BaseJsonEngine
import com.xapps.question_generator.infrastructure.serialization.encoder_and_decoder.KotlinxSerializationDecoder
import com.xapps.question_generator.infrastructure.serialization.encoder_and_decoder.KotlinxSerializationEncoder
import com.xapps.question_generator.infrastructure.serialization.http_message_reader_and_writer.KotlinxSerializationJsonReader
import com.xapps.question_generator.infrastructure.serialization.http_message_reader_and_writer.KotlinxSerializationJsonWriter
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class WebFluxSerializationConfig(
    private val codecVerifier: CodecVerifier
) : WebFluxConfigurer {

    @Bean
    fun kotlinJson(): Json = BaseJsonEngine.json

    @Bean
    fun serializersModule(): SerializersModule = BaseJsonEngine.baseModule

    @Bean
    fun serializerResolver(): ReflectiveSerializerResolver =
        ReflectiveSerializerResolver(serializersModule())

    @Bean
    fun kotlinDecoder(
        json: Json,
        resolver: ReflectiveSerializerResolver
    ) = KotlinxSerializationDecoder(json, resolver)

    @Bean
    fun kotlinEncoder(
        json: Json,
        resolver: ReflectiveSerializerResolver
    ) = KotlinxSerializationEncoder(json, resolver)

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        // Remove all other readers/writers
        configurer.readers.clear()
        configurer.writers.clear()

        configurer.readers.add(KotlinxSerializationJsonReader(kotlinJson(), serializerResolver()))
        configurer.writers.add(KotlinxSerializationJsonWriter(kotlinJson(), serializerResolver()))

        // CRITICAL: Remove ALL default codecs (including Jackson)
        configurer.defaultCodecs().enableLoggingRequestDetails(true)

        // Register ONLY kotlinx.serialization codecs as defaults
        val kotlinxSerializationEncoder = kotlinEncoder(kotlinJson(), serializerResolver())
        configurer.defaultCodecs().kotlinSerializationJsonDecoder(kotlinDecoder(kotlinJson(), serializerResolver()))
        configurer.defaultCodecs().kotlinSerializationJsonEncoder(kotlinxSerializationEncoder)

        configurer.defaultCodecs().serverSentEventEncoder(kotlinxSerializationEncoder)

        // Verify
        codecVerifier.verifyJsonReader(configurer.readers)
        codecVerifier.verifyJsonWriter(configurer.writers)
    }
}