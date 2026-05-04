# Stage 1: Build
FROM amazoncorretto:21-alpine AS builder
WORKDIR /app

RUN apk add --no-cache bash curl sed

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

ENV MAVEN_OPTS="-Dhttp.keepAlive=false \
                -Dmaven.wagon.http.retryHandler.count=3 \
                -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
                -Dmaven.wagon.http.ssl.insecure=true \
                -Dmaven.wagon.http.ssl.allowall=true \
                -Dmaven.wagon.http.ssl.ignore.validity.dates=true \
                -Djava.net.preferIPv4Stack=true"

COPY src ./src
RUN ./mvnw clean package -DskipTests --no-transfer-progress

# Stage 2: Runtime
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
