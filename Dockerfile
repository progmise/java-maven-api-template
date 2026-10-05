# Build the jar: ./mvnw -DskipTests package  →  docker build -t <image> .
FROM eclipse-temurin:21.0.12_8-jre-alpine@sha256:1a29e1fe337eb28b5bec30f0ee8ed29f0ff80ab6f75dcf9313efe82911065a52 AS builder
COPY target/*.jar /tmp/
RUN java -Djarmode=layertools -jar /tmp/*.jar extract --destination /tmp/app

FROM eclipse-temurin:21.0.12_8-jre-alpine@sha256:1a29e1fe337eb28b5bec30f0ee8ed29f0ff80ab6f75dcf9313efe82911065a52
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
