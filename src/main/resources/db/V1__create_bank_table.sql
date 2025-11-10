CREATE TABLE bank
(
    id             UUID PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    bic            VARCHAR(11)  NOT NULL UNIQUE,
    country        CHAR(2)      NOT NULL,
    routing_number VARCHAR(50),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version        BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_bank_name_country UNIQUE (name, country)
);
