# ADR-0001 — Record architecture decisions

**Status:** Accepted · **Date:** 2026-08-06

## Context

EmailAutomata is built across many small commits. Decisions made early —
package layout, the response contract, where delivery status lives — constrain
everything after them. Without a record, the reasoning survives only in the
author's head, and anyone reading the repository later (a reviewer, a future
maintainer, the author in three months) sees the outcome without the argument.

Commit messages describe *what changed*. They are a poor place for *why this
and not the alternative*, because they are scattered and unsearchable.

## Decision

Significant architectural decisions are recorded as numbered Markdown files in
`docs/adr/`, using Nygard's format: Context, Decision, Consequences.

A decision qualifies if reversing it would require touching multiple features.
Routine implementation choices do not.

ADRs are immutable. A reversal is recorded as a new ADR that supersedes the
previous one.

## Consequences

**Positive** — the reasoning behind the structure is legible to anyone reading
the repository. Rejected alternatives are documented, so they are not silently
re-litigated later.

**Negative** — a small ongoing cost per significant decision, and a risk of
drift if ADRs are written after the fact rather than alongside the change.