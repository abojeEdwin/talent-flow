FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

COPY app/pom.xml app/pom.xml
RUN mvn -f app/pom.xml -B -DskipTests dependency:go-offline

COPY app/src app/src
RUN mvn -f app/pom.xml -B clean package -DskipTests && cp app/target/app-*.jar /tmp/app.jar


FROM eclipse-temurin:21-jre

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"

COPY --from=builder /tmp/app.jar /app/app.jar

EXPOSE 80

CMD ["sh", "-c", "exec java -jar /app/app.jar --server.port=${PORT:-80}"]
