CREATE TABLE IF NOT EXISTS shopping_store_products
(
    product_id          UUID            PRIMARY KEY,
    product_name        VARCHAR(255)    NOT NULL,
    description         TEXT            NOT NULL,
    image_src           VARCHAR(512)    NOT NULL,
    quantity_state      VARCHAR(50)     NOT NULL CHECK (quantity_state IN ('ENDED', 'FEW', 'ENOUGH', 'MANY')),
    product_state       VARCHAR(50)     NOT NULL CHECK (product_state IN ('ACTIVE', 'DEACTIVATE')),
    product_category    VARCHAR(50)     NOT NULL CHECK (product_category IN ('LIGHTING', 'CONTROL', 'SENSORS')),
    price               DECIMAL(10, 2)  NOT NULL CHECK (price >= 0)
    );