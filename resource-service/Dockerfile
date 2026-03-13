FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8084
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8084/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=50.0", "-XX:InitialRAMPercentage=25.0", "-XX:+UseContainerSupport", "-XX:+UseG1GC", "-jar", "app.jar"]
