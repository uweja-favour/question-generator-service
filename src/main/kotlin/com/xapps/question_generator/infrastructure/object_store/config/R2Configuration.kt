package com.xapps.question_generator.infrastructure.object_store.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@Configuration
class R2Configuration {

    @Bean
    fun s3Client(): S3Client {
        return S3Client.builder()
            .endpointOverride(
                URI.create("https://310806d043cc8354d08b93a6d7450940.r2.cloudflarestorage.com")
            )
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        "376f4409c4afd582e9c65439423ae3d8",
                        "22391b402c2b9ff72117ca732d12a1010a83b2ea3974cac3d025deb5af49b26e"
                    )
                )
            )
            .region(Region.of("auto"))
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build()
            )
            .build()
    }
}