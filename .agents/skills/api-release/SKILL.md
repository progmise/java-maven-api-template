---
name: api-release
description: Release this API — version bump, tag -> GitHub Actions -> Docker Hub image -> GitHub Release -> Vercel deploy (pro)
argument-hint: "[change summary]"
allowed-tools:
  - read
  - edit
  - exec
  - grep
  - glob
permissions:
  allow:
    - Read(pom.xml)
    - Read(AGENTS.md)
  ask:
    - Write(pom.xml)
    - Exec(./mvnw *)
    - Exec(git *)
---

# Skill: API Release

## Description
Drives a **release** of this microservice. Publishing is fully automated:
running the **Release** workflow (manual dispatch on `main`) validates the
version, runs the CI checks (incl. image build + scans), pushes the image to
Docker Hub (`:version` + `:latest`), creates the git tag + GitHub Release and
deploys to Vercel.

## When to Use
- A change is ready to ship as a new deployed version.
- You need to redeploy an already-released version (use the **Deploy**
  workflow instead — no version bump needed).

## Preconditions
- Repo secrets configured (`DOCKER_USERNAME`, `DOCKER_TOKEN`; optional
  `VERCEL_TOKEN`) and vars (`VERCEL_ORG_ID`, `VERCEL_PROJECT_ID`,
  `DEPLOY_ENVIRONMENTS`) — see README *One-time setup*.
- Working tree green (`build-and-test` skill) before bumping.

---

## Step 1: Decide the version bump
- **Patch** (`x.y.+1`): fixes, dependency patches, docs.
- **Minor** (`x.+1.0`): backwards-compatible features/endpoints.
- **Major** (`+1.0.0`): contract-breaking changes — coordinate consumers.

Confirm the target version with the user.

## Step 2: Bump version
Edit `<version>` in `pom.xml` (and `CHANGELOG.md`). In the same change, update
`AGENTS.md`/`README` version references so docs stay in sync.

## Step 3: Build, test
`./mvnw -B -ntp verify` must be green. Optionally verify the image build:
`docker build -t test .`.

## Step 4: Commit & merge
```bash
git commit -m "<description>"
# PR → development → merge, then PR development → main → merge
```
`<version>` in `pom.xml` must already hold the release version on `main`.

## Step 5: Run the Release workflow
- Actions → **Release** → *Run workflow* on `main`. It validates the version
  (`--kind api`: fails if not on `main`, tag exists or SNAPSHOT; no Maven
  Central check — APIs publish images), runs CI, publishes the image and
  deploys each env in `DEPLOY_ENVIRONMENTS` (default `["pro"]`).
- Without `VERCEL_TOKEN`/`VERCEL_PROJECT_ID` the deploy job is skipped —
  image + release still ship.

## Step 6: Verify
- Image: `docker.io/<DOCKER_USERNAME>/<repo>:<version>` on Docker Hub.
- GitHub Release/tag `<version>` created.
- Vercel deployment URL in the deploy job summary; hit `/actuator/health`.
