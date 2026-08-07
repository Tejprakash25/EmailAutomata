-- V3 — Recipients and recipient lists.
--
-- A recipient belongs to one user, carries a per-recipient merge-field map,
-- and may optionally sit in one named list.

CREATE TABLE recipient_lists (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    user_id     BIGINT        NOT NULL,
    name        VARCHAR(140)  NOT NULL,
    created_at  DATETIME(6)   NOT NULL,
    updated_at  DATETIME(6)   NOT NULL,

    CONSTRAINT pk_recipient_lists PRIMARY KEY (id),
    CONSTRAINT fk_recipient_lists_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_recipient_lists_user_name UNIQUE (user_id, name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE recipients (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    user_id      BIGINT        NOT NULL,
    list_id      BIGINT        NULL,
    email        VARCHAR(320)  NOT NULL,
    display_name VARCHAR(140)  NULL,
    -- Per-recipient merge values, e.g. {"firstName":"Ada","role":"Engineer"}.
    fields       JSON          NOT NULL,
    created_at   DATETIME(6)   NOT NULL,
    updated_at   DATETIME(6)   NOT NULL,

    CONSTRAINT pk_recipients PRIMARY KEY (id),
    CONSTRAINT fk_recipients_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    -- A list deletion detaches its recipients rather than deleting them.
    CONSTRAINT fk_recipients_list
        FOREIGN KEY (list_id) REFERENCES recipient_lists (id) ON DELETE SET NULL,
    -- One address per user; different users may each hold the same address.
    CONSTRAINT uk_recipients_user_email UNIQUE (user_id, email(191))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_recipients_user_created ON recipients (user_id, created_at DESC);
CREATE INDEX idx_recipients_list ON recipients (list_id);