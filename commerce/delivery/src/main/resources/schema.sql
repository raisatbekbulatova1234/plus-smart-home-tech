CREATE TABLE IF NOT EXISTS addresses
(
    address_id      UUID            PRIMARY KEY,
    country         VARCHAR(255)    NOT NULL,
    city            VARCHAR(255)    NOT NULL,
    street          VARCHAR(255)    NOT NULL,
    house           VARCHAR(255)    NOT NULL,
    flat            VARCHAR(255)    NOT NULL DEFAULT '',
    CONSTRAINT uk_address UNIQUE (country, city, street, house, flat)
);

CREATE TABLE IF NOT EXISTS deliveries
(
    delivery_id         UUID            PRIMARY KEY,
    from_address        UUID            NOT NULL,
    to_address          UUID            NOT NULL,
    order_id            UUID            NOT NULL,
    delivery_state      VARCHAR(50)     NOT NULL
    CHECK (delivery_state IN ('CREATED', 'IN_PROGRESS', 'DELIVERED', 'FAILED', 'CANCELLED')),
    CONSTRAINT fk_delivery_from_address FOREIGN KEY (from_address) REFERENCES addresses(address_id),
    CONSTRAINT fk_delivery_to_address FOREIGN KEY (to_address) REFERENCES addresses(address_id)
);