FROM gradle:9.1.0-jdk21 AS Builder
WORKDIR /home/gradle/src
COPY . .
RUN gradle :drone:bootJar --no-daemon

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=Builder /home/gradle/src/drone/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]