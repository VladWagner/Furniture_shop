CREATE TABLE `furniture_shop`.`orders_products_variants`
(
    `id`                 BIGINT NOT NULL,
    `product_variant_id` INT    NOT NULL,
    `order_id`           BIGINT NOT NULL,
    `products_count`     INT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    INDEX                `fk_product_variants_idx` (`product_variant_id` ASC) VISIBLE,
    INDEX                `fk_orders_idx` (`order_id` ASC) VISIBLE,
    CONSTRAINT `fk_product_variants`
        FOREIGN KEY (`product_variant_id`)
            REFERENCES `furniture_shop`.`variants_product` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
    CONSTRAINT `fk_orders`
        FOREIGN KEY (`order_id`)
            REFERENCES `furniture_shop`.`orders` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION
);
