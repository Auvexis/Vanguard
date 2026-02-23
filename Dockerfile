# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/vanguard-*.jar app.jar

# Standard Spring Boot port
EXPOSE 8080

# Run with the production profile by default for the image
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
