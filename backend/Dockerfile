# ============================================================================
# Azreal (POC-Match) Backend — Multi-stage Production Dockerfile (Java 21 LTS)
# ============================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy Maven descriptor first to leverage Docker layer cache
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source and build executable JAR
COPY src ./src
RUN mvn -B package -DskipTests -Dcheckstyle.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
