FROM gradle:8.13.0-jdk17 AS builder
WORKDIR /home/gradle/project
COPY . .

ENV JAVA_HOME=/usr/local/openjdk-17
ENV PATH=$JAVA_HOME/bin:$PATH

RUN ls

RUN cd KeepMyPlanet \
    && chmod +x ./gradlew \
    && ./gradlew :composeApp:wasmJsBrowserDistribution

ARG KMP_SERVER_URL
RUN if [ -f build/dist/wasmJs/productionExecutable/composeApp.js ]; then \
      sed -i "s|http://localhost:1904|${KMP_SERVER_URL}|g" build/dist/wasmJs/productionExecutable/composeApp.js; \
    else \
      echo "composeApp.js not found, skipping sed"; \
    fi

FROM nginx:alpine
COPY --from=builder /home/gradle/project/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

