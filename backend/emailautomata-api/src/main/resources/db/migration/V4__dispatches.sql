-- V4 — Dispatches and their per-recipient fan-out.
--
-- A dispatch is one composed send. dispatch_recipients holds one row per
-- addressee with the content rendered specifically for them — and, from a
-- later migration, that row's individual delivery outcome.

CREATE TABLE dispatches (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    user_id       BIGINT        NOT NULL,
    template_id   BIGINT        NULL,
    subject       VARCHAR(255)  NOT NULL,
    body          MEDIUMTEXT    NOT NULL,
    status        VARCHAR(20)   NOT NULL,
    recipient_count INT         NOT NULL DEFAULT 0,
    scheduled_at  DATETIME(6)   NULL,
    created_at    DATETIME(6)   NOT NULL,
    updated_at    DATETIME(6)   NOT NULL,

    CONSTRAINT pk_dispatches PRIMARY KEY (id),
    CONSTRAINT fk_dispatches_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    -- A template can be deleted without erasing the history of what it sent.
    CONSTRAINT fk_dispatches_template
        FOREIGN KEY (template_id) REFERENCES email_templates (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_dispatches_user_created ON dispatches (user_id, created_at DESC);
CREATE INDEX idx_dispatches_status ON dispatches (status);

CREATE TABLE dispatch_recipients (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    dispatch_id     BIGINT        NOT NULL,
    recipient_id    BIGINT        NULL,
    email           VARCHAR(320)  NOT NULL,
    display_name    VARCHAR(140)  NULL,
    -- Content rendered for this specific addressee, frozen at compose time so
    -- later edits to the template or recipient never rewrite what was sent.
    rendered_subject VARCHAR(255) NOT NULL,
    rendered_body   MEDIUMTEXT    NOT NULL,
    delivery_status VARCHAR(20)   NOT NULL,
    failure_reason  VARCHAR(500)  NULL,
    delivered_at    DATETIME(6)   NULL,
    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NOT NULL,

    CONSTRAINT pk_dispatch_recipients PRIMARY KEY (id),
    CONSTRAINT fk_dispatch_recipients_dispatch
        FOREIGN KEY (dispatch_id) REFERENCES dispatches (id) ON DELETE CASCADE,
    -- The recipient may later be deleted; the sent record survives.
    CONSTRAINT fk_dispatch_recipients_recipient
        FOREIGN KEY (recipient_id) REFERENCES recipients (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_dispatch_recipients_dispatch ON dispatch_recipients (dispatch_id);
CREATE INDEX idx_dispatch_recipients_delivery ON dispatch_recipients (delivery_status);