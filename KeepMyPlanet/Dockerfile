FROM gradle:8.13-jdk17-focal AS builder
WORKDIR /home/gradle/project
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :server:installDist --no-daemon -Dorg.gradle.jvmargs="-Xmx1536M"

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /home/gradle/project/server/build/install/server .
EXPOSE 1904
CMD ["./bin/server"]