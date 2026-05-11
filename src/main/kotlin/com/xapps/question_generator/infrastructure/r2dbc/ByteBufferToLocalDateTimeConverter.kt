//package com.xapps.question_generator.infrastructure.r2dbc
//
//import org.springframework.core.convert.converter.Converter
//import org.springframework.data.convert.ReadingConverter
//import java.nio.ByteBuffer
//import java.time.LocalDateTime
//import java.time.ZoneOffset
//
///**
// * Converts a [ByteBuffer] containing a timestamp in **milliseconds since the Unix epoch**
// * into a [LocalDateTime] in UTC.
// *
// * This converter is primarily used with R2DBC when timestamps are stored as raw bytes (8-byte long)
// * in the database. It reads the milliseconds, splits them into seconds and nanoseconds,
// * and returns a precise [LocalDateTime] representation in UTC.
// *
// * Example:
// * ```
// * val buffer = ByteBuffer.allocate(8).putLong(System.currentTimeMillis())
// * buffer.flip()
// * val localDateTime = ByteBufferToLocalDateTimeConverter.convert(buffer)
// * ```
// *
// * Notes:
// * - Expects exactly 8 bytes in the buffer representing a `Long` timestamp in milliseconds.
// * - The conversion discards any sub-millisecond precision.
// * - UTC is assumed; no timezone conversion is performed.
// */
//@ReadingConverter
//object ByteBufferToLocalDateTimeConverter : Converter<ByteBuffer, LocalDateTime> {
//    override fun convert(source: ByteBuffer): LocalDateTime {
//        // Duplicate buffer to avoid modifying original position
//        val buffer = source.duplicate()
//        buffer.rewind()
//
//        // Ensure buffer contains enough bytes for a Long
//        if (buffer.remaining() < java.lang.Long.BYTES) {
//            throw IllegalArgumentException("ByteBuffer too short to contain a Long timestamp")
//        }
//
//        val millis = buffer.long
//        val seconds = millis / 1000
//        val nanos = ((millis % 1000) * 1_000_000).toInt()
//
//        return LocalDateTime.ofEpochSecond(seconds, nanos, ZoneOffset.UTC)
//    }
//}
