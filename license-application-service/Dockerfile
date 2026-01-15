FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# security: create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# copy built jar from build stage
COPY target/license-application-service-*.jar app.jar

EXPOSE 8080

# jvm settings for container environments
ENV JAVA_OPTS="\
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75 \
  -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
