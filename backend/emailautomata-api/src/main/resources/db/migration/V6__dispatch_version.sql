-- V6 — Optimistic locking for dispatches.
--
-- The scheduler and a manual send can both target the same dispatch. A version
-- column lets the second writer detect it lost the race and back off, so a
-- dispatch is never sent twice.

ALTER TABLE dispatches
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER status;