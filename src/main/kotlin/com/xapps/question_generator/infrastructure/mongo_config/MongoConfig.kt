package com.xapps.question_generator.infrastructure.mongo_config

import com.xapps.dto.job.JobStatus
import com.xapps.time.types.KotlinDuration
import com.xapps.time.types.KotlinInstant
import kotlinx.serialization.json.Json
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.milliseconds

@Configuration
class MongoConfig(
    private val json: Json
) {

    @Bean
    fun customConversions(): MongoCustomConversions {
        return MongoCustomConversions(
            listOf(
                KotlinInstantReadConverter(),
                KotlinInstantWriteConverter(),
                KotlinDurationReadConverter(),
                KotlinDurationWriteConverter(),
                JobStatusWriteConverter(json),
                JobStatusReadConverter(json)
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

@WritingConverter
class JobStatusWriteConverter(
    private val json: Json
) : Converter<JobStatus, String> {
    override fun convert(source: JobStatus): String {
        return json.encodeToString(JobStatus.serializer(), source)
    }
}

@ReadingConverter
class JobStatusReadConverter(
    private val json: Json
) : Converter<String, JobStatus> {
    override fun convert(source: String): JobStatus {
        return json.decodeFromString(JobStatus.serializer(), source)
    }
}