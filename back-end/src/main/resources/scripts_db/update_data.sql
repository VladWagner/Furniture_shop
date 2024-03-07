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

-- Временные скрипты для создания FK constraint'ов

-- products_views FK
alter table products_views
    modify visitor_id bigint unsigned not null;
alter table furniture_shop.products_views
add constraint products_views_visitors_id_fk
        foreign key (visitor_id) references visitors (id);

-- categories_views FK
alter table categories_views
    modify visitor_id bigint unsigned not null;
alter table furniture_shop.categories_views
    add constraint categories_views_visitors_id_fk
        foreign key (visitor_id) references visitors (id);

-- customers FK
alter table customers
    modify visitor_id bigint unsigned not null;
alter table furniture_shop.customers
    add constraint customers_visitors_id_fk
        foreign key (visitor_id) references visitors (id);

-- Индекс для проверки уникальности создаваемой записи скидки. Скидка должна быть уникальна по: % дате начала и окончания
CREATE UNIQUE INDEX unique_idx_discounts ON furniture_shop.discounts (percentage, starts_at, ends_at);