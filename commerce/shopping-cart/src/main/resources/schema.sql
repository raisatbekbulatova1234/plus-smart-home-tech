CREATE TABLE IF NOT EXISTS shopping_carts
(
    shopping_cart_id    UUID DEFAULT    gen_random_uuid() PRIMARY KEY,
    username            VARCHAR(255)    NOT NULL UNIQUE,
    state               VARCHAR(50)     NOT NULL CHECK (state IN ('ACTIVE', 'DEACTIVATE'))
);

CREATE TABLE IF NOT EXISTS shopping_cart_products
(
    shopping_cart_id    UUID    NOT NULL REFERENCES shopping_carts (shopping_cart_id) ON DELETE CASCADE,
    product_id          UUID    NOT NULL,
    quantity            BIGINT  NOT NULL CHECK (quantity > 0),
    CONSTRAINT SHOPPING_CART_PRODUCTS_PK PRIMARY KEY (shopping_cart_id, product_id)
);