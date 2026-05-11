//package com.xapps.question_generator.infrastructure.r2dbc
//
//import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration
//import io.asyncer.r2dbc.mysql.MySqlConnectionFactory
//import io.r2dbc.pool.ConnectionPool
//import io.r2dbc.pool.ConnectionPoolConfiguration
//import io.r2dbc.spi.ConnectionFactory
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
//import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
//import java.time.Duration
//
//@Configuration
//class R2dbcConfig : AbstractR2dbcConfiguration() {
//
//    @Bean
//    override fun connectionFactory(): ConnectionFactory {
//        // Build a MySQL connection factory with a larger request queue
//        val config = MySqlConnectionConfiguration.builder()
//            .host("localhost")
//            .port(3306)
//            .username("root")
//            .password("Sentry_Password")
//            .database("question_generation_dev_db")
////            .maxPendingRequests(2048) // increase from default 128
//            .build()
//
//        val poolConfig = ConnectionPoolConfiguration.builder(MySqlConnectionFactory.from(config))
//            .maxSize(20)       // max simultaneous connections
//            .maxIdleTime(Duration.ofMinutes(30))
//            .build()
//
//        return ConnectionPool(poolConfig)
//    }
//
//    @Bean
//    override fun r2dbcCustomConversions(): R2dbcCustomConversions {
//        val converters = listOf(
//            KotlinInstantToLocalDateTimeConverter,
//            LocalDateTimeToKotlinInstantConverter,
//            ByteBufferToLocalDateTimeConverter
//        )
//        return R2dbcCustomConversions(storeConversions, converters)
//    }
//}
