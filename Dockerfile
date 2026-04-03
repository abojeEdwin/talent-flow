FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY app/pom.xml app/pom.xml
RUN mvn -f app/pom.xml -B -DskipTests dependency:go-offline

COPY app/src app/src
RUN mvn -f app/pom.xml -B clean package -DskipTests && cp app/target/app-*.jar /tmp/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

COPY --from=build /tmp/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar /app/app.jar"]
