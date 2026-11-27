FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy contracts needed by protobuf-maven-plugin
COPY contracts /app/contracts
COPY live-service /app/live-service

WORKDIR /app/live-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/live-service/target/*.jar app.jar
EXPOSE 8087 9097
ENTRYPOINT ["java", "-jar", "app.jar"]
