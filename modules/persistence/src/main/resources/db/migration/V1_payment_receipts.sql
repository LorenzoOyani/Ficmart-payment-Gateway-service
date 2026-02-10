CREATE TABLE payment_receipts
(
    id           BIGINT PRIMARY KEY,
    order_id     VARCHAR(64) NOT NULL,
    customer_id  VARCHAR(64) NOT NULL,
    amount_cents BIGINT      NOT NULL,
    currency     VARCHAR(3)  NOT NULL,
    status       VARCHAR(20) NOT NULL
),
    bank_reference VARCHAR(128),
    issued_at      TIMESTAMP   NOT NULL
)


CREATE TABLE payments
(
    id             BIGINT PRIMARY KEY,
    order_id       VARCHAR(64) NOT NULL,
    customer_id    VARCHAR(64) NOT NULL,
    amount_cents   BIGINT      NOT NULL,
    currency       VARCHAR(3)  NOT NULL,
    status         VARCHAR(20) NOT NULL,
    bank_reference VARCHAR(128),
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP   NOT NULL DEFAULT NOW()
);

