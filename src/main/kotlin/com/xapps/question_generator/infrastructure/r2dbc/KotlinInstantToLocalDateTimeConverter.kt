//package com.xapps.question_generator.infrastructure.r2dbc
//
//import org.springframework.core.convert.converter.Converter
//import org.springframework.data.convert.ReadingConverter
//import org.springframework.data.convert.WritingConverter
//import java.time.LocalDateTime
//import java.time.ZoneOffset
//import kotlin.time.Instant as KotlinInstant
//
//// ----------------- Writing Converter -----------------
//@WritingConverter
//object KotlinInstantToLocalDateTimeConverter : Converter<KotlinInstant, LocalDateTime> {
//    override fun convert(source: KotlinInstant): LocalDateTime {
//        return LocalDateTime.ofEpochSecond(
//            source.epochSeconds,
//            source.nanosecondsOfSecond,
//            ZoneOffset.UTC
//        )
//    }
//}
//
//// ----------------- Reading Converter -----------------
//@ReadingConverter
//object LocalDateTimeToKotlinInstantConverter : Converter<LocalDateTime, KotlinInstant> {
//    override fun convert(source: LocalDateTime): KotlinInstant {
//        val epochSecond = source.toEpochSecond(ZoneOffset.UTC)
//        val nano = source.nano
//        return KotlinInstant.fromEpochSeconds(epochSecond, nano)
//    }
//}
