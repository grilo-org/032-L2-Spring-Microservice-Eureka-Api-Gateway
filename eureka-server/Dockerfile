FROM maven:3.8.4-openjdk-11-slim AS build
WORKDIR /app
COPY ./pom.xml /app/pom.xml
COPY ./src /app/src

RUN mvn dependency:go-offline
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8761
HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD wget -q -T 3 -s http://localhost:8761/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
