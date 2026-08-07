-- V2 — Email templates.
--
-- A template belongs to exactly one user and carries the merge fields it
-- declares, parsed from its subject and body at write time.

CREATE TABLE email_templates (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    user_id      BIGINT        NOT NULL,
    name         VARCHAR(140)  NOT NULL,
    subject      VARCHAR(255)  NOT NULL,
    body         MEDIUMTEXT    NOT NULL,
    -- Declared merge fields, e.g. ["firstName","role"]. Read back whole and
    -- never filtered on, so a JSON column is the right fit — no index needed.
    placeholders JSON          NOT NULL,
    created_at   DATETIME(6)   NOT NULL,
    updated_at   DATETIME(6)   NOT NULL,

    CONSTRAINT pk_email_templates PRIMARY KEY (id),
    CONSTRAINT fk_email_templates_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    -- A user cannot have two templates with the same name; different users can.
    CONSTRAINT uk_email_templates_user_name UNIQUE (user_id, name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- Every list query is "this user's templates, newest first".
CREATE INDEX idx_email_templates_user_created ON email_templates (user_id, created_at DESC);