ALTER TABLE `furniture_shop`.`orders`
DROP FOREIGN KEY `fk_orders_product_variants`;
ALTER TABLE `furniture_shop`.`orders`
DROP COLUMN `products_amount`,
DROP COLUMN `product_variant_id`,
DROP INDEX `fk_orders_product_variants_idx` ;
;
