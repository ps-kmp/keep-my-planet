FROM gradle:8.13.0-jdk17 AS builder
WORKDIR /home/gradle/project
ARG API_BASE_URL
COPY build.gradle.kts settings.gradle.kts gradle.properties gradlew ./
COPY gradle ./gradle/
COPY composeApp/build.gradle.kts ./composeApp/
COPY shared/build.gradle.kts ./shared/
RUN ./gradlew :composeApp:dependencies --no-daemon
COPY composeApp/src ./composeApp/src
COPY shared/src ./shared/src
RUN ./gradlew :composeApp:wasmJsBrowserDistribution --no-daemon
RUN sed -i "s|__API_BASE_URL__|${API_BASE_URL:-}|g" composeApp/build/dist/wasmJs/productionExecutable/index.html

FROM nginx:1.25-alpine
RUN rm /etc/nginx/conf.d/default.conf
COPY frontend/nginx.conf /etc/nginx/conf.d/keepmyplanet.conf
COPY --from=builder /home/gradle/project/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html
EXPOSE 10000
CMD ["nginx", "-g", "daemon off;"]