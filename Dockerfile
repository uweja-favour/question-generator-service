# ─────────────────────────────────────────────
# 1. BUILD STAGE
# ─────────────────────────────────────────────
FROM gradle:8.7-jdk21 AS build

WORKDIR /app

COPY . .

RUN ./gradlew :question-generator-service:bootJar --no-daemon


# ─────────────────────────────────────────────
# 2. RUNTIME STAGE
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/question-generator-service/build/libs/*.jar app.jar

# Railway will override this via PORT, but we still declare a fallback
EXPOSE 8080

# FORCE dev profile (equivalent to your bootRun --args)
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT --spring.profiles.active=dev"]