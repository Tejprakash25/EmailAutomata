# ADR-0003 — Uniform API response envelope

**Status:** Accepted · **Date:** 2026-08-06

## Context

Clients need to distinguish success from failure, and to handle failure in a
way that is more specific than matching on a message string. Two common
approaches exist: return the payload bare and rely entirely on HTTP status, or
wrap every response in a consistent envelope.

Bare payloads mean the client writes different unwrapping logic per endpoint
and has no structured place for a machine-readable error code or field-level
validation detail.

## Decision

Every endpoint returns `ApiResponse<T>`:

```json
{ "success": true,  "data": { }, "timestamp": "..." }
{ "success": false, "error": { "code": "...", "message": "...",
                               "details": { } }, "timestamp": "..." }
```

`error.code` is a stable SCREAMING_SNAKE identifier the client branches on.
`error.message` is user-facing text. `error.details` carries field-level
validation errors keyed by field name.

HTTP status codes remain semantically correct — the envelope supplements them,
it does not replace them.

## Consequences

**Positive** — the client has exactly one unwrapping path (`apiClient`) and one
error type (`ApiRequestError`). Validation errors map directly onto form fields
without per-endpoint parsing. The global exception handler produces the failure
shape in one place, so no controller writes error-handling code.

**Negative** — a small payload overhead per response, and a deviation from
strict REST convention that must be documented for API consumers. Callers who
ignore `success` and read `data` blindly will get `null` rather than an
exception, so the wrapper client is the only sanctioned access path.