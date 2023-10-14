# ---- Build Stage ----
# Use an official Maven base image with JDK 17
FROM maven:3.8.3-openjdk-17 as build

# Set the working directory in the Docker image
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application, skip running tests
RUN mvn clean package -DskipTests

# ---- Run Stage ----
FROM openjdk:17-jdk-alpine AS runtime

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8082

# Command to run the application
CMD ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]
