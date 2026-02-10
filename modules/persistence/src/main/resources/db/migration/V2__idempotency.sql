CREATE TABLE idempotency_records
(
    id              BIGINT PRIMARY KEY    DEFAULT gen_random_uuid(),
    operation       VARCHAR(64)  NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    request_hash    VARCHAR(128) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    response_body   JSONB,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMP
);

CREATE UNIQUE INDEX ux_idem_operation_key
    ON idempotency_records (idempotency_key);

CREATE INDEX idx_idem_expires_at
    ON idempotency_records (expires_at);
