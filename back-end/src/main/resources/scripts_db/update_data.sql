-- Задать значения кодов заказов
update orders
set orders.code = RAND()*(10000000000-10000000)+10000000
where orders.id < 15;

-- Изменить флаги удаления в вариантах товаров
update variants_product
set variants_product.is_deleted = false
where is_deleted is null;

-- Изменить флаги удаления в товарах
update products
set products.is_deleted = false
where is_deleted is null;