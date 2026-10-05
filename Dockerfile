# Self-contained image: source → fat jar → layered runtime.
# Used by `docker build`, docker-compose AND Vercel (Vercel builds the root
# Dockerfile from source — no separate Dockerfile.vercel needed).
# Base images are pinned tag+digest so builds (and Trivy CSA results) are
# reproducible — bump them deliberately.
FROM maven:3.9.16-eclipse-temurin-21-alpine@sha256:308cba8b638ed7e4658cea3f8399066219466211c805f6d5728c3c9c7614661b AS build
WORKDIR /app
COPY pom.xml ./
COPY .mvn ./.mvn
RUN mvn -B -ntp -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21.0.12_8-jre-alpine@sha256:1a29e1fe337eb28b5bec30f0ee8ed29f0ff80ab6f75dcf9313efe82911065a52 AS layers
COPY --from=build /app/target/*.jar /tmp/
RUN java -Djarmode=layertools -jar /tmp/*.jar extract --destination /tmp/app

FROM eclipse-temurin:21.0.12_8-jre-alpine@sha256:1a29e1fe337eb28b5bec30f0ee8ed29f0ff80ab6f75dcf9313efe82911065a52
# Bump packages with known fixes beyond the pinned base (CSA findings)
RUN apk upgrade --no-cache libexpat
ARG EXTRACTED=/tmp/app
WORKDIR /opt/app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=layers ${EXTRACTED}/dependencies/ ./
COPY --from=layers ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=layers ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=layers ${EXTRACTED}/application/ ./

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
