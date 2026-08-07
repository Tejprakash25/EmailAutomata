-- V5 — Record when a dispatch finished sending.
--
-- sent_at is set once the whole dispatch has been processed, giving history and
-- the dashboard a single completion timestamp per send.

ALTER TABLE dispatches
    ADD COLUMN sent_at DATETIME(6) NULL AFTER scheduled_at;