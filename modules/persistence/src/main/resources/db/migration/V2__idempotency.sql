CREATE TABLE idempotency_table
(
    idempotency_key    VARCHAR(255) PRIMARY KEY,

    endpoint           VARCHAR(255) NOT NULL,
    request_hash       VARCHAR(128) NOT NULL,
    merchant_id        VARCHAR(100) NOT NULL,
    operation          VARCHAR(80)  NOT NULL,

    response_body      TEXT,

    idempotency_status VARCHAR(40)  NOT NULL,

    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expired_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    locked_at          TIMESTAMPTZ,

    CONSTRAINT uq_idem UNIQUE (idempotency_key, endpoint)
);

CREATE INDEX idx_idempotency_key
    ON idempotency_table (idempotency_key);

CREATE INDEX idx_idempotency_expired_at
    ON idempotency_table (expired_at);

CREATE INDEX idx_idempotency_merchant_operation
    ON idempotency_table (merchant_id, operation);

CREATE INDEX idx_idempotency_status
    ON idempotency_table (idempotency_status);