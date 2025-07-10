FROM gradle:8.13-jdk17-focal AS builder
WORKDIR /home/gradle/project
COPY .. .
RUN chmod +x ./gradlew
RUN ./gradlew :composeApp:wasmJsBrowserProductionWebpack

FROM nginx:alpine
COPY --from=builder /home/gradle/project/composeApp/build/dist/wasmJs/productionExecutable/ /usr/share/nginx/html/
EXPOSE 80