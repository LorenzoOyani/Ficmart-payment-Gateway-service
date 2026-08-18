CREATE TABLE payments
(
    id                       UUID PRIMARY KEY      DEFAULT gen_random_uuid(),

    order_id                 VARCHAR(100) NOT NULL,
    customer_id              VARCHAR(100) NOT NULL,
    merchant_id              VARCHAR(100) NOT NULL,

    payment_status           VARCHAR(40)  NOT NULL,

    amount                   BIGINT       NOT NULL,
    currency                 VARCHAR(3)   NOT NULL,

    authorization_reference  VARCHAR(255),
    provider_transaction_id  VARCHAR(255),
    stripe_payment_intent_id VARCHAR(255),
    stripe_latest_event_id   VARCHAR(255),
    last_charge_id           VARCHAR(255),

    failure_code             VARCHAR(100),
    failure_message          TEXT,

    authorized_at            TIMESTAMPTZ,
    captured_at              TIMESTAMPTZ,
    voided_at                TIMESTAMPTZ,
    refunded_at              TIMESTAMPTZ,
    expires_at               TIMESTAMPTZ,

    attempt_count            INTEGER      NOT NULL DEFAULT 0,
    next_retry_attempt       INTEGER      NOT NULL DEFAULT 0,

    created_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT ck_payments_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_payments_currency_upper CHECK (currency = upper(currency)),
    CONSTRAINT uq_payments_merchant_order UNIQUE (merchant_id, order_id)
);

CREATE INDEX idx_payments_merchant_id
    ON payments (merchant_id);

CREATE INDEX idx_payments_order_id
    ON payments (order_id);

CREATE INDEX idx_payments_status
    ON payments (payment_status);

CREATE INDEX idx_payments_stripe_payment_intent_id
    ON payments (stripe_payment_intent_id);

CREATE INDEX idx_payments_provider_transaction_id
    ON payments (provider_transaction_id);
