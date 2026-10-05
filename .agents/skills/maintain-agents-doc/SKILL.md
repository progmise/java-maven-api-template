---
name: maintain-agents-doc
description: Reconcile this library's AGENTS.md with the actual codebase (package map, conventions, auto-config registrations, version, consumers)
argument-hint: "[section to focus on, or 'all']"
allowed-tools:
  - read
  - edit
  - grep
  - glob
  - exec
permissions:
  allow:
    - Read(src/**)
    - Read(pom.xml)
    - Read(AGENTS.md)
  ask:
    - Write(AGENTS.md)
---

# Skill: Maintain AGENTS.md

## Description
Keeps the service's canonical documentation (`AGENTS.md`) **in sync with the real
code**. This is an on-demand **audit/reconcile** tool — not a README generator.
Use it after large changes, onboarding, or when docs may have drifted. For
routine changes, prefer updating `AGENTS.md` inline via the `promote-to-lib` /
`library-release` skills (docs-as-code).

## When to Use
- After adding/removing/moving classes or packages.
- After a version bump or dependency change.
- Periodically, to catch accumulated drift.

## Scope
Reconcile the section in `$ARGUMENTS`, or `all`. Things to verify against the
code:

1. **Package map** — must match the actual top-level packages under
   `src/main/java` and describe notable classes.
2. **Conventions** — still match how classes are actually written
   (auto-config beans conditional, dependency scopes, API style).
3. **Auto-configurations** — the doc must list every entry in
   `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
   with its beans and activation conditions.
4. **Version / consuming** — the version mentioned matches `pom.xml`;
   the dependency example uses the current coordinate
   (`io.github.progmise:<artifact>:<version>`).
5. **Consumers** — cross-check by searching the workspace for the artifact in
   consumers' `build.gradle.kts`/`pom.xml`; add/remove repos (names/URLs, not
   local paths).

## Process
1. Enumerate the real state: list `src/main/java` packages/classes, read
   `pom.xml` `<version>`, read the `AutoConfiguration.imports` file,
   grep `@ConditionalOnProperty`/`@ConfigurationProperties`.
2. Diff against each targeted `AGENTS.md` section.
3. Apply **minimal, targeted edits** — do not restructure the document or invent
   content; only reconcile with reality.
4. Report what was out of date and what you changed; list anything ambiguous as
   a suggestion rather than guessing.

## Rules
- Documentation only — never change source code from this skill.
- Preserve the existing document structure, tone and headings.
- *Consumers* lists repo names/URLs, not machine-specific checkout paths.
- Do not bump the service version here (that belongs to `library-release`).
