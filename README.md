# java-maven-api-template

Template for **progmise** Spring Boot microservices — Java 21, Maven,
hexagonal package layout, [api-commons](https://github.com/progmise/api-commons)
shared infrastructure, Docker image, and thin callers to the
[reusable-workflows](https://github.com/progmise/reusable-workflows) `@v1`
pipelines.

## What's inside

| Piece | Notes |
|---|---|
| Java 21 + Spring Boot 3 + Maven wrapper | `./mvnw` — no local Maven needed |
| Hexagonal layout | `domain` / `application.ports` / `application.usecases` / `infrastructure.adapters` |
| `api-commons` | Error contract, validators, `Cache`/`RCache`, `FeatureToggleHelper`, `ApiExceptionHandler` (auto-configured) |
| Togglz | JDBC-backed feature flags — see `domain/FeatureToggle` + `TogglzConfig` (auto-disabled without a `DataSource`) |
| Redis cache | Spring Data Redis + Redisson; fail-open |
| Database | Any JDBC store via `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` (Postgres driver included; swap for others). Remove `spring-boot-starter-data-jpa` entirely if the API has no DB |
| Swagger | springdoc UI at `/swagger-ui.html`; static contract in `docs/swagger.yaml` |
| Sentry | `sentry-spring-boot-starter-jakarta`; active only when `SENTRY_DSN` is set |
| Docker | `Dockerfile` (layered jar, alpine, non-root); `docker-compose.yml` for a local stack (app + postgres + redis) |
| Vercel | `Dockerfile.vercel` — Vercel auto-detects it at the project root and builds the image from source |
| CI/CD | Thin callers in `.github/workflows` → `progmise/reusable-workflows@…@v1` |

## Use this template

1. **Use this template** on GitHub → name the repo after the API (e.g.
   `loans-api`).
2. Rename the base package `io.github.progmise.api` and the `artifactId`/
   `name`/`description` in `pom.xml`.
3. Replace the `Ping` example (port → use case → controller → test) with the
   real first endpoint, following the same path.
4. Update `docs/swagger.yaml`, this README and `application.yml`'s
   `spring.application.name`.

## Local development

```bash
cp .env.example .env          # fill DB_*/REDIS_* — or just use compose:
docker compose up --build     # app on :8080 + postgres + redis, no published image needed

./mvnw -B -ntp verify         # full build + tests (hermetic: H2, no Redis/Sentry)
./mvnw spring-boot:run        # run with the env vars from your shell
```

## One-time setup (CI/CD)

Repository **secrets**: `DOCKER_TOKEN` (Docker Hub push); optional
`VERCEL_TOKEN`, `GRAFANA_OTLP_AUTH`.
Repository **variables**: `DOCKER_USERNAME` (Docker Hub namespace — public
info, kept as var so image names aren't masked in logs), `VERCEL_ORG_ID`,
`VERCEL_PROJECT_ID` (deploy is skipped when unset), `DEPLOY_ENVIRONMENTS`
(JSON list, default `["pro"]` — e.g. `["cert","pre","pro"]`),
`GRAFANA_OTLP_ENDPOINT`.

Image name on Docker Hub = `<DOCKER_USERNAME>/<repo-name>`.

## Release & deploy

- **Release** (manual, `main`): bump `<version>` in `pom.xml`, merge to
  `main`, run *Actions → Release*. Validates, tests, scans, pushes
  `:version` + `:latest` to Docker Hub, creates the GitHub Release/tag and
  deploys each configured env (PRO-only by default) via Vercel.
- **Deploy** (manual): redeploy any released `version` without a new build —
  *Actions → Deploy → version*.
- **Integration** (on merge): same CI plus publishes the image as
  `:<sha>` and `:edge`/` :latest`.
- **CI Checks** (on PR): build, tests, coverage, SAST (Semgrep), SCA +
  container scan (Trivy).

Vercel runs the OCI image built from `Dockerfile.vercel`; production deploys
use `--prod` (env `pro`), other entries in `DEPLOY_ENVIRONMENTS` deploy as
previews. See the `api-release` skill for the full procedure.
