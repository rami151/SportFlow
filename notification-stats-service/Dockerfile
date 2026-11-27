FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY notification-stats-service /app/notification-stats-service

WORKDIR /app/notification-stats-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/notification-stats-service/target/*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
