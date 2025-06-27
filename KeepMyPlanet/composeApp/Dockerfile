FROM gradle:8.13.0-jdk17 AS builder

WORKDIR /home/gradle/project

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew :composeApp:wasmJsBrowserDistribution

RUN sh -c 'sed -i "s|http://localhost:1904|${BACKEND_URL}|g" ./composeApp/build/dist/wasmJs/productionExecutable/composeApp.js'

FROM nginx:1.25-alpine

COPY --from=builder /home/gradle/project/composeApp/build/dist/wasmJs/productionExecutable/ /usr/share/nginx/html

COPY ./composeApp/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80