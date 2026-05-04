FROM amazoncorretto:21-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8083
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8083/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
