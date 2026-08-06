-- V1 — Baseline schema.
--
-- Flyway owns the schema; Hibernate is configured to validate against it and
-- never to generate it. Every subsequent change ships as a new versioned
-- migration, so the schema history is auditable and reproducible.

CREATE TABLE users (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    email         VARCHAR(320)  NOT NULL,
    password_hash VARCHAR(100)  NOT NULL,
    display_name  VARCHAR(120)  NOT NULL,
    status        VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME(6)   NOT NULL,
    updated_at    DATETIME(6)   NOT NULL,

    CONSTRAINT pk_users PRIMARY KEY (id),
    -- 320 is the RFC-5321 maximum address length; the unique index is declared
    -- on a prefix because utf8mb4 pushes the full column past InnoDB's key limit.
    CONSTRAINT uk_users_email UNIQUE (email(191))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_users_created_at ON users (created_at);