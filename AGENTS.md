# AGENTS.md

Guide for working on APIs generated from **java-maven-api-template** —
progmise Spring Boot microservices.

## Architecture

Hexagonal (ports & adapters), minimal:

```
domain/                  — entities, FeatureToggle enum (no framework deps)
application/
  ports/input/           — input port interfaces (what the app offers)
  ports/output/          — output port interfaces (what the app needs)
  usecases/              — @Service implementations of input ports
infrastructure/
  adapters/input/rest/   — @RestController, DTOs (request/response live here)
  adapters/output/       — persistence/cache clients
  config/                — @Configuration (TogglzConfig, SwaggerConfig)
```

Rules:
- Controllers never contain business logic — they call an input port.
- Domain/application never import `org.springframework.web` or adapters.
- Errors use the api-commons contract `{"errors":[{code,message,level,description}]}`
  via `ApiExceptionHandler` + `BadRequestException`/`NotFoundException`/…
  (already auto-configured — do **not** redeclare the bean).
- Feature flags: add a constant to `domain/FeatureToggle` (value = snake_case
  name), evaluate with `FeatureToggleHelper.isActive(FeatureToggle.X)`
  (auto-configured by api-commons; fail-safe `false`). State persists in the
  API's own datasource via `FeatureToggleStateRepository` (Togglz JDBC).
- Generic, multi-API candidates (validators, helpers, patterns duplicated in
  2+ services) belong in `api-commons` — use the `promote-to-lib` skill.

## Conventions

- Java 21, Maven wrapper, Lombok (`@RequiredArgsConstructor`, `@Slf4j`),
  `lombok.config` present.
- Config only through env vars — `application.yml` holds placeholders with
  sane local defaults; never hardcode or commit secrets. New vars go in
  `.env.example` (no values) + README table.
- Static API contract in `docs/swagger.yaml` kept in sync with controllers;
  springdoc serves live docs at `/swagger-ui.html`.
- Sentry: env-gated (`SENTRY_DSN`); nothing to configure in code.
- Tests are hermetic — H2 + no Redis/Sentry via `src/test/resources/application.yml`.
  One test per use case / endpoint at minimum.

## CI/CD

All logic lives in `progmise/reusable-workflows` (`@v1`, `secrets: inherit`).
Callers here are thin — keep them that way. Pipeline order:
`Setup → Build artifact → Build image → SAST ‖ SCA ‖ CSA → Tracing → Summary`;
release adds `Validate → CI → Publish Image → Release` before Tracing/Summary —
**never deploys**; deploys run via Deploy (manual) or the orchestrator.
Deploy envs: `vars.DEPLOY_ENVIRONMENTS` (default `["pro"]`). `Publish Image`
and `Deploy` are skipped when `DOCKER_USERNAME`/`VERCEL_PROJECT_ID` are unset —
a fresh template CI stays green without credentials.

## Verify before done

```bash
./mvnw -B -ntp verify
docker build -t api:dev .        # when touching Dockerfile/runtime config
```

## Branches

`main` (releases) + `development` (integration). Work lands on
`<type>/<snake_description>` → PR to `development` → PR to `main`.
Types: `feature/`, `fix/`, `chore/`, `docs/`, `refactor/`.
