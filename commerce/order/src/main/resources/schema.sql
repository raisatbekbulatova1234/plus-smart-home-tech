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

CREATE TABLE IF NOT EXISTS orders
(
    order_id            UUID                   PRIMARY KEY,
    shopping_cart_id    UUID                   NOT NULL,
    username            VARCHAR(255)           NOT NULL,
    to_address          UUID                   NOT NULL,
    from_address        UUID,
    payment_id          UUID,
    delivery_id         UUID,
    state               VARCHAR(50)            NOT NULL CHECK (state IN (    'NEW',
                                                                             'ON_PAYMENT',
                                                                             'ON_DELIVERY',
                                                                             'DONE',
                                                                             'DELIVERED',
                                                                             'ASSEMBLED',
                                                                             'PAID',
                                                                             'COMPLETED',
                                                                             'DELIVERY_FAILED',
                                                                             'ASSEMBLY_FAILED',
                                                                             'PAYMENT_FAILED',
                                                                             'PRODUCT_RETURNED',
                                                                             'CANCELED')),
    delivery_weight     DOUBLE PRECISION       CHECK (delivery_weight >= 1),
    delivery_volume     DOUBLE PRECISION       CHECK (delivery_volume >= 1),
    fragile             BOOLEAN,
    total_price         DECIMAL(10, 2)         CHECK (total_price >= 0),
    delivery_price      DECIMAL(10, 2)         CHECK (delivery_price >= 0),
    product_price       DECIMAL(10, 2)         CHECK (product_price >= 0),
    CONSTRAINT fk_delivery_to_address FOREIGN KEY (to_address) REFERENCES addresses(address_id)
);

CREATE TABLE IF NOT EXISTS order_products
(
    order_id            UUID    NOT NULL REFERENCES orders (order_id) ON DELETE CASCADE,
    product_id          UUID    NOT NULL,
    quantity            BIGINT  NOT NULL CHECK (quantity > 0),
    CONSTRAINT          ORDER_PRODUCTS_PK PRIMARY KEY (order_id, product_id)
);