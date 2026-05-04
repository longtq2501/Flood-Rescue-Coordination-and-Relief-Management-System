FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
# Retry wrapper for Maven downloads to avoid transient network failures (3 attempts)
RUN set -eux; \
		rm -rf /root/.m2/repository || true; \
		n=0; \
		until [ "$n" -ge 5 ]; do \
			mvn -U dependency:go-offline -B -q && break || { n=$((n+1)); echo "mvn attempt $n failed, retrying..."; sleep 5; }; \
		done; \
		if [ "$n" -ge 5 ]; then echo "Maven dependency fetch failed after 5 attempts"; exit 1; fi
COPY src ./src
RUN mvn clean package -DskipTests -q -T 1C

FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8085/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
