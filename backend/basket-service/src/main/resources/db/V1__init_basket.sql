CREATE TABLE IF NOT EXISTS basket (
    id BIGSERIAL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS basket_item (
    id BIGSERIAL PRIMARY KEY,
    basket_id BIGINT NOT NULL REFERENCES basket(id) ON DELETE CASCADE,
    ean VARCHAR(64) NOT NULL,
    name TEXT NOT NULL,
    image_url TEXT,
    qty INT NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_basket_item_basket_ean
    ON basket_item (basket_id, ean)M