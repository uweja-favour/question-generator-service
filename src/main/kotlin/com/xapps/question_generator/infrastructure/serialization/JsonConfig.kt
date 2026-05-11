package com.xapps.question_generator.infrastructure.serialization

import com.xapps.question_generator.infrastructure.serialization.http_message_reader_and_writer.KotlinxSerializationJsonReader
import com.xapps.question_generator.infrastructure.serialization.http_message_reader_and_writer.KotlinxSerializationJsonWriter
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class JsonConfig(
    private val json: Json,
    private val serializerResolver: ReflectiveSerializerResolver
) : WebFluxConfigurer {

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        // Clear default readers/writers
        configurer.apply {
            // Clear default codecs
            this.readers.clear()
            this.writers.clear()

            // Add our reactive codecs
            this.readers.add(KotlinxSerializationJsonReader(json, serializerResolver))
            this.writers.add(KotlinxSerializationJsonWriter(json, serializerResolver))
        }
    }
}