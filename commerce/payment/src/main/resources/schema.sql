CREATE TABLE IF NOT EXISTS payments
(
    payment_id       UUID               PRIMARY KEY,
    order_id         UUID               NOT NULL,
    product_price    DECIMAL(10, 2)     NOT NULL CHECK (product_price >= 0),
    delivery_total   DECIMAL(10, 2)     NOT NULL CHECK (delivery_total >= 0),
    fee_total        DECIMAL(10, 2)     NOT NULL CHECK (fee_total >= 0),
    state            VARCHAR(50)        NOT NULL CHECK (state IN ('PENDING', 'SUCCESS', 'FAILED'))
    );