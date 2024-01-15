CREATE TABLE furniture_shop.baskets_products_variants
(
    id                 INT    NOT NULL AUTO_INCREMENT,
    basket_id          BIGINT NOT NULL,
    product_variant_id INT    NOT NULL,
    products_count     INT NULL,
    PRIMARY KEY (id),
    INDEX                fk_bpv_baskets_idx (basket_id ASC) VISIBLE,
    INDEX                fk_bpv_variants_product_idx (product_variant_id ASC) VISIBLE,
    CONSTRAINT fk_bpv_baskets
        FOREIGN KEY (basket_id)
            REFERENCES furniture_shop.baskets (id)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
    CONSTRAINT fk_bpv_variants_product
        FOREIGN KEY (product_variant_id)
            REFERENCES furniture_shop.variants_product (id)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION
);