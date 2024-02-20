-- таблица производителей
delete from producers where producers.id > 0;
ALTER TABLE producers AUTO_INCREMENT = 1;

-- таблица категорий
delete from categories where categories.id > 0;
ALTER TABLE categories AUTO_INCREMENT = 1;

delete from categories where categories.id > 32;
ALTER TABLE categories AUTO_INCREMENT = 32;

-- таблица шаблонных категорий
delete from subcategories where subcategories.id > 7;
ALTER TABLE subcategories AUTO_INCREMENT = 7;

-- таблица атрибутов товаров по категориям
delete from attributes_categories where attributes_categories.id > 0;
ALTER TABLE attributes_categories AUTO_INCREMENT = 1;

-- таблица значений атрибутов товаров по категориям
delete from products_prices where products_prices.id > 0;
ALTER TABLE products_prices AUTO_INCREMENT = 1;

-- таблица вариантов исполнения товаров
delete from variants_product where variants_product.id > 0;
ALTER TABLE variants_product AUTO_INCREMENT = 1;
ALTER TABLE variants_product AUTO_INCREMENT = 187;

update variants_product set variants_product.price = (ROUND(RAND()*(50000-15000)+15000,0)/1000)*1000
where variants_product.id < 94;

-- таблица товаров
delete from products where products.id > 1000;
ALTER TABLE products AUTO_INCREMENT = 1;

-- таблица пользователей
delete from verification_tokens where verification_tokens.id > 0;
ALTER TABLE verification_tokens AUTO_INCREMENT = 1;

delete from users where users.id > 7;
ALTER TABLE users AUTO_INCREMENT = 7;

delete from users_passwords where users_passwords.id > 7;
ALTER TABLE users_passwords AUTO_INCREMENT = 7;


--

ALTER TABLE users AUTO_INCREMENT = 7;
ALTER TABLE users_passwords AUTO_INCREMENT = 7;


delete from products_images where products_images.id > 69;
ALTER TABLE products_images AUTO_INCREMENT = 69;

delete from variants_product where variants_product.id > 187;
ALTER TABLE variants_product AUTO_INCREMENT = 187;

-- Восстановление последних корректных значений заполненных вручную
select attributes_values.id from attributes_values where attributes_values.product_id = 50 order by id desc limit 1;
delete from attributes_values where attributes_values.product_id > 55;
ALTER TABLE attributes_values AUTO_INCREMENT = 975;

delete from products where products.id > 55;
ALTER TABLE products AUTO_INCREMENT = 55;

-- Очистка таблицы просмотров категорий
delete from categories_views where categories_views.id > 7;
ALTER TABLE categories_views AUTO_INCREMENT = 7;

-- Очистка таблицы корзин
delete from baskets where baskets.id > 0;
ALTER TABLE baskets AUTO_INCREMENT = 1;

-- Очистка таблицы с корзинами и вариантами товаров
delete from baskets_products_variants where baskets_products_variants.id > 0;
ALTER TABLE baskets_products_variants AUTO_INCREMENT = 1;

-- Очистка таблицы с корзинами и вариантами товаров
delete from orders_products_variants where orders_products_variants.id > 0;
ALTER TABLE orders_products_variants AUTO_INCREMENT = 1;

-- Очистка таблицы токенов подтверждения почты пользователя
delete from verification_tokens where verification_tokens.id > 0;
ALTER TABLE verification_tokens AUTO_INCREMENT = 1;

-- Очистка таблицы токенов
delete from password_reset_token where password_reset_token.id > 0;
ALTER TABLE password_reset_token AUTO_INCREMENT = 1;

-- Очистка таблицы посещений по дням
delete from daily_visits where daily_visits.id > 36;
ALTER TABLE daily_visits AUTO_INCREMENT = 36;

-- Очистка таблицы многие ко многим атрибутов товаров
delete from attributes_categories where attributes_categories.id > 316 and attribute_id = 41;
ALTER TABLE attributes_categories AUTO_INCREMENT = 316;

-- Очистка таблицы атрибутов товаров
delete from products_attributes where products_attributes.id > 40;
ALTER TABLE products_attributes AUTO_INCREMENT = 40;
