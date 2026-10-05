---
name: promote-to-lib
description: Promote duplicated/generic code from consumer services into api-commons, then dedupe the consumers
argument-hint: "[what to promote, e.g. 'pagination' or a class name]"
allowed-tools:
  - read
  - edit
  - exec
  - grep
  - glob
permissions:
  allow:
    - Read(AGENTS.md)
  ask:
    - Write(src/**)
    - Exec(./mvnw *)
    - Exec(git *)
---

# Skill: Promote to Lib

## Description
Moves **generic code that is duplicated across consumer services** into this
shared library, then updates each consumer to use the library version and deletes
its local copy. Turns copy-paste into a single shared implementation.

## When to Use
- The same generic class/pattern exists (near-)identically in more than one
  consumer.
- A consumer has generic code that clearly belongs in api-commons.

## Inputs
- **Consumers**: default set = *Consumers* in `AGENTS.md`. **Confirm local
  checkout paths with the user** (they vary per machine); warn about any not
  cloned. Do not assume absolute paths.
- **Candidate**: what to promote (`$ARGUMENTS`).

---

## Step 1: Confirm the candidate is promotable
- It is **generic** (no consumer-specific domain logic).
- It is **not already** in the library (check the relevant package).
- Compare the copies across consumers (normalize package names) to confirm they
  are equivalent; note any per-consumer differences to reconcile.
- Its API must work from **Java and Kotlin** — plain Java surface, no
  Kotlin-only constructs (no `inline`/`reified`/extension-only access). For
  generics prefer `Class<T>` or Jackson `TypeReference<T>` parameters.

## Step 2: Add to the library
- Place it in the right package (`validator/`, `exception/`, `util/`, `config/`,
  `infrastructure/`, ...).
- Keep intra-library imports consistent; add unit tests (port the consumers'
  tests, adjusting packages).
- Respect backward compatibility — this is an additive change.
- **Update `AGENTS.md`** in the same change: add the promoted classes to the
  package map (docs-as-code). Add a new consumer to *Consumers* if you
  discovered one.

## Step 3: Release the library
Use the `library-release` skill (bump per the version strategy — usually
**patch/minor**; run `backward-compatibility-check` if unsure) and
`./mvnw -B -ntp install` so consumers
can resolve it locally before the Release workflow is run.

## Step 4: Update each consumer
For every confirmed consumer:
- Bump the library dependency to the new version.
- **Delete** the local copy (main + its moved tests). Keep consumer-specific
  subclasses.
- Redirect imports to the library packages — **including fully-qualified
  references in non-source files** (`.yml`/`.properties`), which break silently
  at runtime.
- Kotlin consumers calling into Java APIs: no named arguments on Java methods,
  positional constructors, SAM lambdas are fine.
- Build + test green (`./gradlew build` or `mvn test` depending on the consumer).
- Commit per repo and push.

## Step 5: Report
- What was promoted and to which package.
- Library version produced.
- Per-consumer: updated / build status / anything not cloned.

## Rules & pitfalls
- Search **all file types** for old FQNs, not just sources.
- Use `@ConditionalOnMissingBean` for any promoted Spring bean so consumers can
  override.
- Do not promote consumer-specific logic; only the generic core.
- Keep `AGENTS.md` -> *Consumers* updated if you discover a new consumer.
