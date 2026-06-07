CREATE TABLE IF NOT EXISTS warehouse_products
(
    product_id          UUID        PRIMARY KEY,
    fragile             BOOLEAN     NOT NULL           DEFAULT FALSE,
    width               FLOAT       NOT NULL           CHECK (width >= 1),
    height              FLOAT       NOT NULL           CHECK (height >= 1),
    depth               FLOAT       NOT NULL           CHECK (depth >= 1),
    weight              FLOAT       NOT NULL           CHECK (weight >= 1),
    quantity            BIGINT      NOT NULL           DEFAULT 0 CHECK ( quantity >= 0 )
);