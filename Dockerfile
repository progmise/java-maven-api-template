# Build the jar: ./mvnw -DskipTests package  →  docker build -t <image> .
FROM eclipse-temurin:21-jre-alpine AS builder
COPY target/*.jar /tmp/
RUN java -Djarmode=layertools -jar /tmp/*.jar extract --destination /tmp/app

FROM eclipse-temurin:21-jre-alpine
ARG EXTRACTED=/tmp/app
WORKDIR /opt/app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=builder ${EXTRACTED}/dependencies/ ./
COPY --from=builder ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=builder ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=builder ${EXTRACTED}/application/ ./

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
