FROM gradle:8.13.0-jdk17 AS builder
WORKDIR /home/gradle/project
COPY . .
RUN chmod +x ./gradlew \
 && ./gradlew :composeApp:wasmJsBrowserDistribution

ARG KMP_SERVER_URL
RUN sed -i "s|http://localhost:1904|${KMP_SERVER_URL}|g" \
    build/dist/wasmJs/productionExecutable/composeApp.js

FROM nginx:alpine
COPY --from=builder /home/gradle/project/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
