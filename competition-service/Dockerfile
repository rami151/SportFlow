FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy contracts needed by protobuf-maven-plugin
COPY contracts /app/contracts
COPY competition-service /app/competition-service

WORKDIR /app/competition-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/competition-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
