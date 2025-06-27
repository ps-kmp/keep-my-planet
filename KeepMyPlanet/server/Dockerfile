FROM gradle:8.13.0-jdk17 AS builder
WORKDIR /home/gradle/project
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :server:installDist --no-daemon

FROM amazoncorretto:17-alpine-jdk
WORKDIR /app
COPY --from=builder /home/gradle/project/server/build/install/server ./
EXPOSE 1904
CMD ["./bin/server"]