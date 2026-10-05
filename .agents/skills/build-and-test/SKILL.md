---
name: build-and-test
description: Compile, test, run and dockerize this Java/Maven microservice (JDK 21, Maven wrapper, Dockerfile)
allowed-tools:
  - read
  - exec
  - grep
  - glob
permissions:
  allow:
    - Read(pom.xml)
    - Read(src/**)
  ask:
    - Exec(./mvnw *)
    - Exec(docker *)
---

# Skill: Build and Test — Java/Maven microservice

## Description
Step-by-step guide to compile, test and containerize this Spring Boot service.
It is a **Maven** project built with the wrapper (`./mvnw`) on Java 21.

## When to Use
- Compiling/testing the service for the first time or on a new machine.
- Diagnosing build/test/dependency-resolution failures.
- Building the Docker image locally.

---

## Step 1: Environment
- **JDK 21**. Set `JAVA_HOME` to a JDK 21 before building.
- Confirm the version under test in `pom.xml` (`<version>...</version>`).

## Step 2: Compile
```bash
./mvnw -B -ntp compile
```

## Step 3: Run tests
```bash
./mvnw -B -ntp test
```
Tests are hermetic: H2 in-memory + no Redis/Sentry (`src/test/resources/application.yml`).

## Step 4: Full verify
```bash
./mvnw -B -ntp verify
```
Packages the boot jar with layered jars enabled (required by `Dockerfile`).

## Step 5: Docker image (optional)
```bash
docker build -t java-maven-api-template:dev .
docker compose up --build   # app + postgres + redis, full local stack
```

## Troubleshooting

| Symptom | Root cause | Fix |
|---|---|---|
| `Failed to configure a DataSource` | DB envs unset | `cp .env.example .env` and fill `DB_*`, or `docker compose up postgres` |
| Redis connection refused in logs | Redis not up locally | Harmless — cache is fail-open; or `docker compose up redis` |
| Stale results | Incremental build | `./mvnw -B -ntp clean verify` |

## Notes
- Do **not** bump `version` as part of a build — see the `api-release` skill.
