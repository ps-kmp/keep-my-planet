FROM gradle:8.13-jdk17-focal AS builder
WORKDIR /home/gradle/project
COPY build.gradle.kts settings.gradle.kts gradle.properties gradlew ./
COPY gradle ./gradle/
COPY server/build.gradle.kts ./server/
COPY shared/build.gradle.kts ./shared/
COPY composeApp/build.gradle.kts ./composeApp/
RUN ./gradlew :server:dependencies --no-daemon
COPY server/src ./server/src
COPY shared/src ./shared/src
COPY composeApp/src ./composeApp/src
RUN ./gradlew :server:installDist --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /home/gradle/project/server/build/install/server/ /app/
EXPOSE 10000
CMD ["./bin/server"]