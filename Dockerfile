# -----------------------------
# Stage 1: Build the Maven project
# -----------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy all files
COPY . .

# Build the project (skip tests)
RUN mvn clean package -DskipTests

# -----------------------------
# Stage 2: Run the Spring Boot app
# -----------------------------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
