# Simple runtime-only Dockerfile
# Requires: JAR already built locally with ./mvnw package
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built JAR
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
