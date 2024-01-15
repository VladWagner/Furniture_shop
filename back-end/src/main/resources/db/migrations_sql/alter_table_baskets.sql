-- 05.01.2024: удалить поля внешнего ключа на вариант товара и количества товаров
ALTER TABLE furniture_shop.baskets
DROP
FOREIGN KEY fk_basket_product_variants;
ALTER TABLE furniture_shop.baskets
DROP
COLUMN products_amount,
DROP
COLUMN product_variant_id,
DROP INDEX fk_basket_product_variants_idx;
;

