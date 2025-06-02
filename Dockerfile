# Base image
FROM openjdk:17

# Working directory inside container
WORKDIR /app

# Copy only the built JAR file
COPY build/libs/*.jar app.jar

# Open required ports
EXPOSE 8080 8081

# Run the application
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]