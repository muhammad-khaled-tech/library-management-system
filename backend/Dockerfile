# Build stage: full JDK + Maven wrapper
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Download dependencies in their own layer so code changes don't re-download them
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline

COPY src/ src/
# Tests run in CI (they need Docker for Testcontainers, which a build container doesn't have)
RUN ./mvnw -B -q -DskipTests package

# Runtime stage: JRE only, non-root user
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/target/library-management-system-*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
