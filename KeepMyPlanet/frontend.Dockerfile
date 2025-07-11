FROM gradle:8.13.0-jdk17 AS builder
WORKDIR /home/gradle/project
ARG API_BASE_URL
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :composeApp:wasmJsBrowserDistribution
RUN sed -i "s|__API_BASE_URL__|${API_BASE_URL}|g" composeApp/build/dist/wasmJs/productionExecutable/index.html

FROM nginx:1.25-alpine
RUN rm /etc/nginx/conf.d/default.conf
COPY frontend/nginx.conf /etc/nginx/conf.d/keepmyplanet.conf
COPY --from=builder /home/gradle/project/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html
EXPOSE 10000
CMD ["nginx", "-g", "daemon off;"]