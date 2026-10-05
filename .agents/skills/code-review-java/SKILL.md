---
name: code-review-java
description: Iterative code quality improvement (naming, structure, complexity) for this Java/Maven microservice (production code, tests, or both)
argument-hint: "[scope: 'src/main', 'src/test', 'both', or a specific path/pattern]"
allowed-tools:
  - read
  - edit
  - grep
  - glob
  - exec
permissions:
  allow:
    - Read(src/**)
    - Read(AGENTS.md)
  ask:
    - Write(src/**)
    - Exec(./mvnw *)
---

Act as a **Senior Software Engineer and Code Reviewer**.

Your goal is to **progressively improve code quality** in the specified scope,
considering everything that implies, without breaking existing functionality or
assuming changes outside the current scope.

> **This repository is a microservice**, **not** a microservice. It has no REST
> endpoints and is **not** hexagonal; it exposes Spring Boot auto-configurations,
> base classes, and utilities consumed by other services. Review against
> **library conventions**, not application/hexagonal ones.

## Scope

Review and improve the code in: **$ARGUMENTS**

Valid scopes:
- `src/main` — production code only
- `src/test` — test code only
- `both` — production and test code
- A specific directory or file pattern (e.g., `src/main/java/.../validator/`)

If no scope is specified, ask the user what to review.

When scope is `both`, review production code first, then test code, keeping
changes coordinated (e.g., if a class is renamed, update its tests in the same
iteration).

## Project conventions

Read `AGENTS.md` at the project root before making any changes. Follow its
package map, conventions, and especially the backward-compatibility rules.

## Main objectives

- Improve **readability**, **maintainability**, and **clarity**.
- Prioritize **clear and descriptive names** for variables, methods, classes,
  fields. Avoid unnecessary abbreviations unless widely standard.
- Preserve the current functional behavior **and the public API** of the service.

## Important rules

1. **Backward compatibility is critical** — this library is consumed by other
   services. Never break the public API: do **not** remove/rename public
   classes, methods, or fields, or change public method signatures, without a
   deprecation cycle. Prefer **additive** changes.
2. Do **not** force refactors blocked by the framework, managed dependency
   versions, or hard-to-revert architectural decisions. If blocked, do not
   implement — document them clearly as **suggestions**.
3. Do not introduce over-engineering or unnecessary patterns.
4. Respect the tech stack and general style; improve only when clearly beneficial.
5. **Version bumps** are out of scope — if a change would warrant a release,
   note it (per `AGENTS.md`) but do not bump.

## Production code review criteria

### Naming & readability
- Utility classes: `final class` + private constructor + static methods.
- Auto-configurations: beans gated with `@ConditionalOnProperty` and overridable
  via `@ConditionalOnMissingBean`.
- Names reflect responsibility; no unnecessary abbreviations.

### Library structure & conventions
- Public API must stay consumable from Java **and** Kotlin — no Kotlin-only
  constructs in signatures.
- Public types in signatures ⇒ `compile` dependency scope; container-supplied ⇒
  `provided`; lombok `provided`.
- No business logic or secrets/keys in code or logs.

### Code style
- Early-return over if/else nesting.
- Compact code: no duplicate branches, no unnecessary nesting.
- Program against interfaces (`List`, `Map`) not implementations.
- `Map.of()` for immutable maps, mutable impl only when mutation is needed.
- Methods under ~30 lines; extract if longer.

### Imports
- No wildcard imports (`import ...*`).
- No fully-qualified class names inline; always use imports.

## Test code review criteria

### JUnit 5 conventions
- Test classes and methods are package-private (no `public`).
- Test classes end with `Test`.

### Test naming & structure
- Descriptive names; one behavior per test method.

### Test data & setup
- Shared setup in `@BeforeEach` or helpers; avoid duplicated fixtures.
- Repeated hardcoded values become constants.

### Assertions
- Use `assertAll()` to group related assertions.
- Specific assertions (`assertEquals`, `assertThrows`, `assertNotNull`) over
  generic ones.
- Consider `@ParameterizedTest` when tests differ only in input data.

## Iterations

Perform the work in **2 to 3 iterations**:

1. **Iteration 1 — Readability & Naming**: naming improvements, safe refactors,
   import cleanup, obvious cleanup.
2. **Iteration 2 — Structure & Complexity**: light structural improvements,
   consolidated duplication, better organization.
3. **Iteration 3 (optional) — Polish**: consistency, edge cases, comments where
   they add clear value.

**After each iteration**, run `./mvnw -B -ntp test` to verify nothing is broken.

## Deliverables per iteration

- **Scope reviewed** (production, tests, or both)
- **Changes made** (what and why)
- **Files affected**
- **Suggestions NOT applied**, with the reason (backward-compat, framework, etc.)

## Format

- Be explicit and clear in decisions made.
- Use technical but understandable language.
- Avoid generic responses; show professional judgment in every trade-off.

When ready, start with **Iteration 1**.
