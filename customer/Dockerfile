FROM gradle:9.1.0-jdk21 AS Builder
WORKDIR /home/gradle/src
COPY . .
RUN gradle :customer:bootJar --no-daemon

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=Builder /home/gradle/src/customer/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]