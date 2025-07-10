FROM gradle:8.13-jdk17-focal AS builder
WORKDIR /home/gradle/project
COPY gradle/ /home/gradle/project/gradle/
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties /home/gradle/project/
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :server:installDist

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /home/gradle/project/server/build/install/server/ /app/
COPY --from=builder /home/gradle/project/server/build/resources/main/static /app/static
EXPOSE 10000
CMD ["./bin/server"]