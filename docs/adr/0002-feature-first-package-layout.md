# ADR-0002 — Feature-first package layout

**Status:** Accepted · **Date:** 2026-08-06

## Context

The conventional Spring Boot layout groups classes by technical layer:
`controller/`, `service/`, `repository/`, `dto/`, `entity/`. It is familiar and
requires no explanation.

It also scales badly. With eight features, `service/` holds eight unrelated
classes, and understanding one capability means opening five directories and
mentally filtering each. Coupling between features becomes invisible, because
nothing about the structure signals which classes belong together.

## Decision

Backend packages are organised by capability: