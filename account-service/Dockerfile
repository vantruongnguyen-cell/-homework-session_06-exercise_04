# Giai đoạn 1: Build file .jar bằng Gradle Wrapper của dự án
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src
RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon

# Giai đoạn 2: Chạy ứng dụng
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]