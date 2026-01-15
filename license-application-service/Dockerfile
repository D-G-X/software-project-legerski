# =========================
# Build Stage
# =========================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

# copy pom.xml to get dependencies
COPY pom.xml .
RUN mvn -B -e -C dependency:go-offline

# copy source code
COPY . .

# build application (skip tests, liquibase and codegen generation)
RUN mvn -B clean package \
  -DskipTests \
  -Dliquibase.skip=true \
  -Djooq.codegen.skip=true


# =========================
# Runtime Stage
# =========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# security: create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# copy built jar from build stage
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

# jvm settings for container environments
ENV JAVA_OPTS="\
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75 \
  -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
