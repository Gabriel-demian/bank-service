# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests dependency:go-offline
COPY src ./src
# Genera los *MapperImpl (MapStruct) y el jar
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests clean package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar
ENV JAVA_OPTS=""
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=15s --timeout=3s --retries=5 CMD \
  curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
