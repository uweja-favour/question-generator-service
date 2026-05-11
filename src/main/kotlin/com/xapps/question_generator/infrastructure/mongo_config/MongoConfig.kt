package com.xapps.question_generator.infrastructure.mongo_config

import com.xapps.time.types.KotlinDuration
import com.xapps.time.types.KotlinInstant
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import kotlin.time.Duration.Companion.milliseconds

@Configuration
class MongoConfig {

    @Bean
    fun customConversions(): MongoCustomConversions {
        return MongoCustomConversions(
            listOf(
                KotlinInstantReadConverter(),
                KotlinInstantWriteConverter(),
                KotlinDurationReadConverter(),
                KotlinDurationWriteConverter(),
            )
        )
    }
}

@WritingConverter
class KotlinInstantWriteConverter : Converter<KotlinInstant, Long> {
    override fun convert(source: KotlinInstant): Long =
        source.toEpochMilliseconds()
}

@ReadingConverter
class KotlinInstantReadConverter : Converter<Long, KotlinInstant> {
    override fun convert(source: Long): KotlinInstant =
        KotlinInstant.fromEpochMilliseconds(source)
}

@WritingConverter
class KotlinDurationWriteConverter : Converter<KotlinDuration, Long> {
    override fun convert(source: KotlinDuration): Long =
        source.inWholeMilliseconds
}

@ReadingConverter
class KotlinDurationReadConverter : Converter<Long, KotlinDuration> {
    override fun convert(source: Long): KotlinDuration =
        source.milliseconds
}