import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    kotlin("plugin.jpa") version "2.3.0"

    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"

    // --- Database / Migration ---
    id("org.flywaydb.flyway") version "9.22.0"
}

group = "com.xapps"
version = "0.0.1-SNAPSHOT"
description = "Self Test Quiz Service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {

    /* =========================================================
     * INTERNAL MODULES
     * ========================================================= */
    implementation(project(":contracts"))
    implementation(project(":platform"))

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    /* =========================================================
     * SPRING BOOT – REACTIVE & SECURITY
     * ========================================================= */

    implementation("org.springframework.boot:spring-boot-starter-webflux") {
        // Remove Jackson Completely
        exclude(group = "com.fasterxml.jackson.core")
        exclude(group = "com.fasterxml.jackson.module")
        exclude(group = "com.fasterxml.jackson.datatype")
    }
//    implementation("org.springframework.boot:spring-boot-starter-web") {
//        exclude(group = "com.fasterxml.jackson.core")
//        exclude(group = "com.fasterxml.jackson.module")
//        exclude(group = "com.fasterxml.jackson.datatype")
//    }
//    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    /* =========================================================
     * KOTLIN / REACTIVE SUPPORT
     * ========================================================= */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

    /* =========================================================
     * SERIALIZATION
     * ========================================================= */

    // Kotlin 2.2 requires serialization 1.7.x+.
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.7.3")

    /* =========================================================
     * AUTH / JWT
     * ========================================================= */
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
//    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    /* =========================================================
     * FILE / DOCUMENT HANDLING
     * ========================================================= */
    implementation("org.apache.pdfbox:pdfbox:2.0.30")
//    implementation("commons-io:commons-io:2.15.1")
    implementation("commons-io:commons-io:2.17.0")

    implementation("org.apache.poi:poi:5.4.0")
    implementation("org.apache.poi:poi-ooxml:5.4.0")
    implementation("org.apache.poi:poi-scratchpad:5.4.0")


    /* =========================================================
     * OCR
     * ========================================================= */
    implementation("net.java.dev.jna:jna:5.13.0")
    implementation("net.sourceforge.tess4j:tess4j:5.9.0")

    /* =========================================================
     * AWS
     * ========================================================= */
    implementation("software.amazon.awssdk:s3:2.25.0")
    implementation("software.amazon.awssdk:sts:2.25.0")

    /* =========================================================
     * RESILIENCE / UTILITIES
     * ========================================================= */
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.1.0")
    implementation("io.github.resilience4j:resilience4j-timelimiter:2.1.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("com.benasher44:uuid:0.8.4")
    implementation("org.jetbrains.kotlinx:atomicfu:0.24.0")
    implementation("com.knuddels:jtokkit:0.5.0")

    /* =========================================================
     * HTTP CLIENTS
     * ========================================================= */

    // Ktor
    val ktor = "3.3.3" // formerly 2.3.12
    implementation("io.ktor:ktor-client-core:$ktor")
    implementation("io.ktor:ktor-client-cio:$ktor")
    implementation("io.ktor:ktor-network:${ktor}")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")
    implementation("io.ktor:ktor-client-logging:$ktor")

    // OkHttp
    val okHttpVersion = "5.3.2"
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

    implementation("com.benasher44:uuid-jvm:0.8.4")

    // Apache Kafka
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.kafka:kafka-clients")

    // Mongo DB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

    /* =========================================================
    * RESILIENCE
    * ========================================================= */
    implementation(platform("io.github.resilience4j:resilience4j-bom:2.1.0"))

    implementation("io.github.resilience4j:resilience4j-retry")
    implementation("io.github.resilience4j:resilience4j-kotlin")
    implementation("io.github.resilience4j:resilience4j-timelimiter")
    implementation("io.github.resilience4j:resilience4j-spring-boot3")

    implementation("software.amazon.awssdk:s3:2.25.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xannotation-default-target=param-property"
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-preview")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    jvmArgs("--enable-preview")
}

configurations.all {
    resolutionStrategy {
        force("commons-io:commons-io:2.17.0")
    }
}