FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy contracts needed by protobuf-maven-plugin
COPY contracts /app/contracts
COPY eligibility-service /app/eligibility-service

WORKDIR /app/eligibility-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/eligibility-service/target/*.jar app.jar
EXPOSE 8086 9096
ENTRYPOINT ["java", "-jar", "app.jar"]
