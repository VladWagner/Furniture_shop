-- Выборка категорий
select
    categories.id,
    ifnull(categories.category_name, '---')     as name,
    ifnull(subcategories.sub_name, '---') as subcategory_name,
    parent_category.category_name  as parent_category

from
    -- Присоединять subcategories нужно ко всем categories, поскольку в categories может не быть записей subcategories
    categories left join subcategories on categories.subcategory_id = subcategories.id
               join categories as parent_category on categories.parent_id = parent_category.id;

select
    ifnull(categories.category_name, s.sub_name) as category
from
    categories left join subcategories s on categories.subcategory_id = s.id

group by category;

-- Выборка всех базовых товаров
select
    products.id,
    product_name,
    description,
    ifnull(categories.category_name,s.sub_name) as category,
    producers.producer_name,
    if(is_available=1,'true','false') as is_available,
    if(show_product=1,'true','false') as show_product
from products join (categories left join subcategories s on categories.subcategory_id = s.id)
                    on products.category_id = categories.id
              join producers on products.producer_id = producers.id;

-- Выборка дочерних и родительских категорий
select
    ifnull(categories.category_name,s.sub_name) as category,
    (select categ.category_name from categories as categ where categ.id = categories.parent_id) as parent_category,
    count(*)
from
    products join (categories left join subcategories s on categories.subcategory_id = s.id) on products.category_id = categories.id
group by category, parent_category;

-- Выборка товаров по категории
select
    products.id,
    product_name,
    description,
    ifnull(categories.category_name,s.sub_name) as category,
    producers.producer_name,
    if(is_available=1,'true','false') as is_available,
    if(show_product=1,'true','false') as show_product
from products join (categories left join subcategories s on categories.subcategory_id = s.id)
                   on products.category_id = categories.id
              join producers on products.producer_id = producers.id
where ifnull(categories.category_name,s.sub_name) like '%тумб%'  ;

-- Выборка всех атрибутов товаров
select
    *
from
    products_attributes;

-- Выборка атрибутов привязанных по категориям от конкретного товара (например категория у диванов)
SET @product_id = 45;
select
    prod_attr.id,
    prod_attr.attr_name,
    categories.id as category_id,
    ifnull(categories.category_name,s.sub_name) as category
from attributes_categories join products_attributes prod_attr on attributes_categories.attribute_id = prod_attr.id
                           join (categories left join subcategories s on categories.subcategory_id = s.id)
                               on attributes_categories.category_id = categories.id
where
    -- Выборка атрибутов по заданной основной категории
    attributes_categories.category_id  = (select categories.id from categories join products p on categories.id = p.category_id where p.id = @product_id)
    or
    -- Если категория задана в таблице повторяющихся категорий, тогда нужно вытаскивать id записей в таблице subcategory_id
    (select categories.subcategory_id from categories where categories.id = attributes_categories.category_id limit 1)
    =
    (select categories.subcategory_id from categories join products p on categories.id = p.category_id where p.id = @product_id);

-- Выборка товаров и характеристик по ним
select
    products.id,
    product_name,
    p_attr.attr_name,
    p_attr.id as attr_id,
    av.txt_values,
    av.int_value
from
    -- Ко всем товарам присоединяем заданные для них атрибуты
    products left join (attributes_values av join products_attributes p_attr on av.attribute_id = p_attr.id)
        on products.id = av.product_id
where p_attr.attr_name like '%прих%' and av.txt_values like 'средний';

-- Выборка повторяющихся материалов для товаров в определённой категории
set @category = 'Пуф';
select
    attributes_values.txt_values,
    attributes_values.int_value,
    attributes_values.float_value,
    count(*)
from
    attributes_values join view_products vp on attributes_values.product_id = vp.id
                      join products_attributes pa on attributes_values.attribute_id = pa.id
where
    (vp.category like CONCAT('%', @category,'%') and pa.attr_name like 'материалы') and
    (txt_values is not null or int_value is not null or float_value is not null)
group by txt_values, int_value, float_value;

-- Выборка конкретных товаров и их характеристик
select
    view_products_features.product_id,
    view_products_features.product_name,
    count(*) as general_amount,
    sum(view_products_features.feature like 'особенности') as materials_amount

from view_products_features
/*where view_products_features.product_name like '%пуф%'*/
group by view_products_features.product_id,
    view_products_features.product_name;

-- Выборка определённых характеристик определённого товара
set @product = 42, @feature_name = 'назначение';
select
    view_products_features.feature,
    view_products_features.feature_value

from
    view_products_features
where view_products_features.product_id = @product and view_products_features.feature like @feature_name;

-- Выборка всех вариантов вместе с характеристик по ним
select
    view_pf.product_name,
    view_pf.feature,
    view_pf.feature_value,
    variants_product.title,
    variants_product.preview_img
from
    variants_product
join
    view_products_features view_pf on variants_product.id = view_pf.product_id
where
    view_pf.category like '%див%';

-- Выборка всех вариантов каждого товара
select
    variants_product.id as variant_id,
    vp.id,
    vp.product_name,
    variants_product.title
from
    variants_product join view_products vp on variants_product.product_id = vp.id;

-- Выборка фотографий опрделённого варианта определённого товара
set @product_id = 58, @variant = '', @product_name = 'Диван Гранд велюр';

select
    vp.product_name,
    vp.category,
    variants_product.title
from products_images join (variants_product join view_products vp on variants_product.product_id = vp.id)
    on variants_product.id = products_images.product_variant_id;

-- Выбор последней релевантной цены для определённой версии товара
select
    v_prod.product_name,
    v_prod.category,
    CONCAT(v_prod.product_name,'.',vp.title) as variant,
    products_prices.price
from
    products_prices join (variants_product vp join view_products v_prod on vp.product_id = v_prod.id)
        on products_prices.product_variant_id = vp.id
    /*products_prices join (variants_product vp join view_products_features vpf on vp.product_id = vpf.product_id)
        on products_prices.product_variant_id = vp.id*/
where
    products_prices.date = (select max(pp.date) from products_prices pp where pp.product_variant_id = products_prices.product_variant_id);

-- Выборка пользователей
select
    users.id,
    users.login,
    ur.role
from users join user_roles ur on users.role_id = ur.id;

-- Выборка товаров с фильтрацией по характеристикам
select distinct
    vp.id,
    variants.id as variant_id,
    vp.product_name,
    vp.show_product,
    variants.price
from
    view_products as vp
    join (products_attributes pa left join attributes_values av on av.attribute_id = pa.id) on av.product_id = vp.id
    join variants_product variants on variants.product_id = vp.id
where (pa.attr_name like 'Высота' and av.int_value between 700 and 2200 or
      pa.attr_name like 'Ширина' and av.int_value between 700 and 2300 or
      pa.attr_name like 'Глубина' and av.int_value between 300 and 1500) and
(select vr.id from variants_product as vr where vr.product_id = vp.id limit 1) = variants.id;

-- Подсчёт кол-ва товаров с фильтрацией по характеристикам
select distinct
    count(variant.id)
from
    view_products as vp
join (products_attributes pa left join attributes_values av on av.attribute_id = pa.id) on av.product_id = vp.id
join variants_product variant on variant.product_id = vp.id
where (pa.attr_name like 'Высота' and av.int_value between 700 and 2200 or
       pa.attr_name like 'Ширина' and av.int_value between 700 and 2300 or
       pa.attr_name like 'Глубина' and av.int_value between 300 and 1500) and
        (select vr.id from variants_product as vr where vr.product_id = vp.id limit 1) = variant.id;

-- Выборка товаров с фильтрацией по характеристикам и ценам
select distinct
    vp.id,
    variants.id as variant_id,
    vp.product_name,
    vp.show_product,
    variants.price
from
    view_products as vp
        join (products_attributes pa left join attributes_values av on av.attribute_id = pa.id) on av.product_id = vp.id
        join variants_product variants on variants.product_id = vp.id
where (pa.attr_name like 'Высота' and av.int_value between 700 and 2200 or
       pa.attr_name like 'Ширина' and av.int_value between 700 and 2300 or
       pa.attr_name like 'Глубина' and av.int_value between 300 and 1500) and
        variants.price between 20000 and 25000 and
        (select min(vr.id) from variants_product as vr where vr.product_id = vp.id) = variants.id;

select
    *
from
    products p join (categories c left join subcategories sub_c on c.subcategory_id = sub_c.id) on p.category_id = c.id
             /*join (products_attributes p_attrs left join attributes_values av on p_attrs.id = av.attribute_id) on av.product_id = p.id*/
where c.id = 8;

-- Выборка кол-ва просмотров по каждой категории
select
    categ.category_name,
    sum(cv.count),
    round(avg(cv.count),0)

from
    categories_views cv join (categories categ left join categories parent_categ on categ.parent_id = parent_categ.id) on cv.category_id = categ.id
group by categ.category_name;

-- Выборка всех просмотров категорий
select
    cw.id as cw_id,
    ifnull(c.category_name, sub_c.sub_name) as category_name,
    c.parent_id,
    cw.count
from categories_views cw join (categories c
                                    left join subcategories sub_c on c.subcategory_id = sub_c.id) on cw.category_id = c.id
where c.id = 2 or parent_id = 2;

-- Выборка всех дочерних категорий
set @categoryId = 1;
with recursive category_tree as (
                -- Выборка родительской категории (базовый узел) НА КАЖДОМ УРОВНЕ РЕКУРСИИ
                select
                    id,
                    category_name,
                    subcategory_id,
                    parent_id
                from
                    categories
                where categories.id = @categoryId
                union all
                -- Выборка дочерних категорий - рекурсивная часть (дочерние узлы и листья)
                -- Т.е. здесь будут выбираться все дочерние категории родительской категории на каждом уровне рекурсии (в глубину и в ширину)
                -- Получится так: Кухни -> выбираем все дочерние элементы -> выбираем дочерние элементы дочерних элементов "кухни" и т.д.,
                -- если конечно parent_id есть в записях
                select
                    c.id,
                    c.category_name,
                    c.subcategory_id,
                    c.parent_id
                -- Главная точка сравнения - parent_id здесь будет проверяться для каждого дочернего элемента
                from categories c join category_tree ct on c.parent_id = ct.id
                )


select
    *
from category_tree;

-- Подсчёт кол-ва просмотров во всех дочерних категориях
set @categoryId = 2;
with recursive category_tree as (
                -- Выборка просмотров родительской категории (базовый узел) НА КАЖДОМ УРОВНЕ РЕКУРСИИ
                select
                    c.id as c_id,
                    c.category_name,
                    cv.count
                from
                    categories_views cv join categories c on c.id = cv.category_id
                where c.id = @categoryId OR c.parent_id = @categoryId
                -- union без all, чтобы исключались дубликаты, которые за счёт двойного условия обязательно появятся
                union
                -- Выборка просмотров дочерних категорий
                select
                    c.id as c_id,
                    c.category_name,
                    cv.count
                -- Главная точка сравнения - parent_id здесь будет проверяться для каждого дочернего элемента
                from categories_views cv join categories c on c.id = cv.category_id
                                         join category_tree ct on c.parent_id = ct.c_id
                )

select
    sum(ct.count) as parent_category_views
from category_tree ct;

select
    c.id
from
    categories c
where
    c.parent_id = :id;

-- Выборка атрибутов под конкретные категории и их максимальных значений
set @categoryId = 11;
with products_in_category as(
    set @categoryId = 8;
    select
        p.id
    from
        products p
    where
       (@categoryId > 0 and p.category_id = @categoryId and p.is_deleted = false and p.show_product = true) or @categoryId <= 0)
       -- p.category_id in (10,11,15) and p.is_deleted = false and p.show_product = true)

select
    av.attribute_id as attributeId,
    prod_attr.attr_name as attributeName,

    MIN(av.int_value) as min,
    MAX(av.int_value) as max,

    av.txt_values as value

from
    attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id
where
     av.product_id in (select pic.id from products_in_category pic) and
     (av.int_value is not null or (av.txt_values is not null and av.txt_values != ''))
group by
    prod_attr.attr_name, av.attribute_id, av.txt_values;


-- Выборка значений атрибутов по конкретному названию
select
    av.attribute_id as attributeId,
    prod_attr.attr_name as attributeName,
    av.int_value,
    av.txt_values

from
    attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id
where
        prod_attr.attr_name like 'Страна-производитель';

-- Тестовая выборка товаров по значениям орпделённых атрибутов
select
    p.id,
    p.category_id,
    p.producer_id,
    producer_name,
    attrVal.attribute_id,
    attrVal.int_value,
    attrVal.txt_values
from
    products as p join attributes_values attrVal on attrVal.product_id = p.id
                 join producers on p.producer_id = producers.id
                 join variants_product pv on pv.product_id = p.id
where
    producers.producer_name like 'BTS' and
    p.id in (select av.product_id from attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id where prod_attr.id = 1 and av.int_value between 500 and 1200) and
    p.id in (select av.product_id from attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id where prod_attr.id = 2 and av.int_value between 200 and 600)
  and
    (p.id in (select av.product_id from attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id where prod_attr.id = 5 and av.txt_values = 'МДФ') or
    p.id in (select av.product_id from attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id where prod_attr.id = 5 and av.txt_values = 'ЛДСП') or
    p.id in (select av.product_id from attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id where prod_attr.id = 5 and av.txt_values = 'Велюр'))
  and
    (p.id in (select av.product_id from attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id where prod_attr.id = 24 and av.txt_values = 'Классическая') or
    p.id in (select av.product_id from attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id where prod_attr.id = 24 and av.txt_values = 'Прямоугольные') or
    p.id in (select av.product_id from attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id where prod_attr.id = 24 and av.txt_values = 'Круглый') or
    p.id in (select av.product_id from attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id where prod_attr.id = 24 and av.txt_values = 'Круглые')
        );

-- Выбрать цены на товары в определённой категории
set @categoryId = 10;
set @categoryIdList = (10,11,15);
select
    MIN(vp.price) as min_set,
    MAX(vp.price) as max_set
from
    products p join variants_product vp on  p.id = vp.product_id
where
    IF(@categoryIdList is not null, p.category_id in @categoryIdList, (@categoryId > 0 and p.category_id = 0) or @categoryId <= 0)
   -- p.category_id in @categoryIdList
   -- (0 > 0 and p.category_id = 0) or 0 <= 0

-- Полнотекстовый поиск через индексы
set @keyword = 'BTS';
select
    *
from products p join producers producer on p.producer_id = producer.id
                join variants_product vp on p.id = vp.product_id
where
    MATCH(p.product_name, p.description) AGAINST (@keyword) or
    MATCH(vp.title) AGAINST (@keyword) or
    MATCH(producer.producer_name) AGAINST (@keyword);

set @keyword = 'диван';
select distinct
    p.id as product_id,
    p.product_name,
    p.description as product_description,
    -- vp.id as variant_id,
    -- vp.title as product_variant_name,
    producer.producer_name

from products p join producers producer on p.producer_id = producer.id
                join variants_product vp on p.id = vp.product_id
where
    (p.product_name like concat('%',@keyword,'%') or
    p.description like concat('%',@keyword,'%') or
    vp.title like concat('%',@keyword,'%') or
    producer.producer_name like concat('%',@keyword,'%')) and p.is_deleted = false and p.show_product = true;

-- Выборка заказов и их детальной информации
select
    orders.id as order_id,
    CONCAT(customer.surname, '.', SUBSTR(customer.name, 1,1), '.', SUBSTR(customer.patronymic, 1,1)) as customer_snp,
    p.id as product_id,
    vp.title as variant_name,
    p.description as product_description,
    vp.price,
    opv.products_count,
    (vp.price * opv.products_count) as summ

from orders join customers customer on customer.id = orders.customer_id
            join order_states os on orders.order_state_id = os.id
            left join (orders_products_variants opv join (variants_product vp join products p on vp.product_id = p.id)
                        on opv.product_variant_id = vp.id)
                on orders.id = opv.order_id;

-- Тестовая выборка с проверкой на null
set @order_state_id = 0, @code = 4237291951;
select
    orders.id,
    orders.code,
    CONCAT(c.surname, '.', SUBSTR(c.name, 1,1), '.', SUBSTR(c.patronymic, 1,1)) as customer_snp,
    c.email
from orders join customers c on orders.customer_id = c.id
where
    c.email = 'user4@gmail.com';
    -- (@code <= 0 or @code is null and @code = orders.code) or orders.code > 0
   -- if(@order_state_id is null or @order_state_id <= 0, (@code is not null and @code > 0 and @code = orders.code), orders.id = @order_state_id)

-- Выборка корзин и их детальной информации
select
    baskets.id as basket_id,
    u.login,
    p.id as product_id,
    vp.id as pv_id,
    vp.title as variant_name,
    p.description as product_description,
    vp.price,
    bpv.products_count,
    (vp.price * bpv.products_count) as summ,
    baskets.added_date

from baskets join users u on baskets.user_id = u.id
             join (baskets_products_variants bpv
                            join (variants_product vp
                                join products p on vp.product_id = p.id) on bpv.product_variant_id = vp.id)
                on baskets.id = bpv.basket_id
where u.id = 3;

-- Выборка всех товаров с их просмотрами
select
    p.id,
    SUM(ifnull(pviews.count,0)) as amount
from products p left join products_views pviews on p.id = pviews.product_id
group by p.id
having
    amount >= 0
order by amount desc;

-- Выборка среднего просмотра для каждого товара
select
    p.id,
    AVG(ifnull(pviews.count,0)) as avg_amount
from products p left join products_views pviews on p.id = pviews.product_id
group by p.id
having
        avg_amount >= 0
order by avg_amount desc;

-- Выборка товаров с максимальным количеством промотров
select
    p.id,
    SUM(ifnull(pviews.count,0)) as amount
from products p left join products_views pviews on p.id = pviews.product_id
group by p.id
having
     amount >= (
     Select  MAX(sums)
     from
         (Select SUM(pviews.count) as sums
          from products p left join products_views pviews on p.id = pviews.product_id
          group by p.id) as views_sums);
with max_views as (select
    p.id,
    SUM(ifnull(pviews.count,0)) as amount
from products p left join products_views pviews on p.id = pviews.product_id
group by p.id
having
     amount >= (
     Select  MAX(sums)
     from
         (Select SUM(pviews.count) as sums
          from products p left join products_views pviews on p.id = pviews.product_id
          group by p.id) as views_sums))

select
    COUNT(*)
from max_views;


-- Выбрать посетителей и просмотренные ими товары
select
    visitors.id,
    visitors.fingerprint,
    p.id,
    p.product_name,
    (select vp.price from variants_product vp where vp.product_id = p.id limit 1) as price,
    -- SUM(ifnull(pv.count,0)) as amount
    pv.count
from visitors join (products_views pv join products p on pv.product_id = p.id) on visitors.id = pv.visitor_id
group by visitors.id, visitors.fingerprint, p.id, p.product_name, price;

-- Количество посещений за
with visistors_in_dates as (select
    v.last_visit_at,
    COUNT(*) as visits_amount
from
    visitors v
where v.last_visit_at between '2023-12-05' and '2024-01-16'
group by v.last_visit_at
)

select
    coalesce(sum(vid.visits_amount), 0)
from visistors_in_dates vid;

-- Выборка просмотров из таблицы daily_visits
select
    dv.date,
    dv.count
from
    daily_visits dv
where dv.date between '2023-12-05' and '2024-01-16';

select
    coalesce(SUM(dv.count), 0)
from
    daily_visits dv
where
    dv.date between '2023-12-05' and '2024-01-16';

-- Выборка количества заказов по дням в определённом периоде
set @order_state_id = null;
select
    DATE(o.order_date),
    COUNT(o.id) as orders_amount,
    SUM(o.sum) as sum
from
    orders o
where
    o.order_date between '2023-12-25' and '2024-01-16' and
    ((@order_state_id is not null and order_date > 0 and o.order_state_id = @order_state_id)
          or @order_state_id is null or @order_state_id <0 )
group by o.order_date;

-- Конверсия из просмотров в заказ товара в определённой категории по дням
set @category_id = null;
with date_and_orders_count as (select
    DATE(o.order_date) as order_date_alias,
    coalesce((select count(v.id)
        from visitors v where v.last_visit_at = order_date_alias group by v.last_visit_at), 0) as visits, -- кол-во визитов в текущую дату
    COUNT(DISTINCT(o.id)) as orders_amount
from orders o join (orders_products_variants opv join
                            (variants_product vp join products p on vp.product_id = p.id)
                            on opv.product_variant_id = vp.id)
        on o.id = opv.order_id
where o.order_date between '2023-09-25' and '2024-01-17' and
    ((@category_id is not null and @category_id > 0 and p.category_id = @category_id)
        or @category_id is null or @category_id < 0 )
group by order_date_alias, visits)

select
    doc.order_date_alias,
    doc.orders_amount,
    doc.visits,
    coalesce(doc.orders_amount/visits, 0) as cvr
from date_and_orders_count doc;

-- Конверсия из просмотров в заказ товара по id
set @product_id = null;
with date_and_orders_count as (
select
    DATE(o.order_date) as order_date_alias,
    coalesce((select count(v.id)
        from visitors v where v.last_visit_at = order_date_alias group by v.last_visit_at), 0) as visits,
    COUNT(DISTINCT(o.id)) as orders_amount
from orders o left join (orders_products_variants opv join
                            (variants_product vp join products p on vp.product_id = p.id)
                            on opv.product_variant_id = vp.id)
        on o.id = opv.order_id
where o.order_date between '2023-09-25' and '2024-01-17' and
    ((@product_id is not null and @product_id > 0 and p.id = @product_id)
        or @product_id is null or @product_id < 0)
group by order_date_alias, visits)


select
    doc.order_date_alias,
    doc.orders_amount,
    doc.visits,
    coalesce(doc.orders_amount/visits*100, 0) as cvr
from date_and_orders_count doc
where doc.visits > doc.orders_amount;

-- Выборка конверсий добавлений в корзину определённого товара
set @product_id = null;
select
    bdc.add_date_alias,
    bdc.addings_amount,
    bdc.visits,
    coalesce(bdc.addings_amount/visits*100, 0) as cvr
from (select
          DATE(b.added_date) as add_date_alias,
          coalesce((select count(v.id)
                    from visitors v where v.last_visit_at = add_date_alias group by v.last_visit_at), 0) as visits,
          COUNT(DISTINCT(b.id)) as addings_amount
      from baskets b left join (baskets_products_variants bpv join
          (variants_product vp join products p on vp.product_id = p.id)
                          on bpv.product_variant_id = vp.id)
                         on b.id = bpv.basket_id
      where b.added_date between '2023-09-25' and '2024-01-17' and
          ((@product_id is not null and @product_id > 0 and p.id = @product_id)
              or @product_id is null or @product_id <= 0)
      group by add_date_alias, visits) as bdc;

-- Частота просмотров товаров на посетителя
with products_views_in_category as (select
    c.id as category_id,
    p_categ.id as parent_id,
    coalesce(c.category_name, sub_c.sub_name) as categ_name,
    (select coalesce(sum(pviews.count),0) from products_views pviews join products p on p.id = pviews.product_id
              where p.category_id = c.id) as products_views,
    (select count(pviews.visitor_id) from products_views pviews join products p on p.id = pviews.product_id
              where p.category_id = c.id) as visitors_amount,
    (select coalesce(sum(pviews.count)/count(pviews.visitor_id),0) from products_views pviews join products p on p.id = pviews.product_id
              where p.category_id = c.id) as frequecny

from categories c left join subcategories sub_c on c.subcategory_id = sub_c.id
                  join categories as p_categ on c.parent_id = p_categ.id)

-- рассчитать частоту
select
    pvic.category_id,
    pvic.parent_id,
    pvic.categ_name,
    pvic.products_views,
    pvic.visitors_amount,
    coalesce(products_views/visitors_amount, 0) as frequency
from
    products_views_in_category pvic;

-- Частота просмотров категорий на посетителя
with categories_views as (select
    c.id as category_id,
    p_categ.id as parent_id,
    coalesce(c.category_name, sub_c.sub_name) as categ_name,
    (select coalesce(sum(cviews.count),0) from categories_views cviews where cviews.category_id = c.id) as products_views,
    (select count(cviews.visitor_id) from categories_views cviews where cviews.category_id = c.id) as visitors_amount,
    (select coalesce(sum(cviews.count)/count(cviews.visitor_id),0) from categories_views cviews where cviews.category_id = c.id) as frequecny

from categories c left join subcategories sub_c on c.subcategory_id = sub_c.id
                  join categories as p_categ on c.parent_id = p_categ.id)

-- рассчитать частоту
select
    cv.category_id,
    cv.parent_id,
    cv.categ_name,
    cv.products_views,
    cv.visitors_amount,
    coalesce(products_views/visitors_amount, 0) as frequency
from
    categories_views cv;

-- Количество заказов каждого товара в категории + фильтр
set @category_id = null, @date_lo = '2023-09-25', @date_hi = '2024-01-17';
set @price_min = null, @price_max = null;
select
    p.id,
    p.product_name,
    pv.title,
    pv.id as pv_id,
    /*count(distinct (p.id)) as orders_amount_product,*/
    count(pv.id) as orders_amount_variant
from
    products p join (orders_products_variants opv join orders o on opv.order_id = o.id
                                                  join variants_product pv on opv.product_variant_id = pv.id)
               on pv.product_id = p.id
where ((@category_id is not null and @category_id > 0 and p.category_id = @category_id)
        or @category_id is null or @category_id < 0 ) and
        (@date_lo is not null and o.order_date >= @date_lo or @date_lo is null) and
        (@date_hi is not null and o.order_date <= @date_hi or @date_hi is null) and
        (@price_min is not null and pv.price >= @price_min or @price_min is null) and
        (@price_max is not null and @price_max > 0 and pv.price <= @price_max or @price_max is null or @price_max <= 0)
group by pv.id/*, p.product_name*/
having orders_amount_variant > 1;

set @date_lo = '2024-01-01', @date_hi = null/*'2024-01-16'*/;
select
    MIN(o.order_date),
    Max(o.order_date)
from
    orders o join order_states os on o.order_state_id = os.id
where
    o.order_state_id = 2;

-- Выборка товаров с подсчётом кол-ва
select
    p.id,
    p.product_name,
    count(distinct (o.id)) as orders_amount
from
    orders_products_variants opv join orders o on opv.order_id = o.id
                                 join (variants_product pv join (products p join categories c on p.category_id = c.id) on pv.product_id = p.id)
                                     on opv.product_variant_id = pv.id
group by p.id,p.product_name;

-- Выборка товаров с наибольшим кол-вом ФАКТОВ добавлений в корзину
with max_views as (select
    p.id,
    p.product_name, pv.title,pv.id as pv_id,
    COUNT(pv.id) as amount
    from baskets_products_variants bpv join baskets b on b.id = bpv.basket_id
                                       join (variants_product pv join (products p join categories c on p.category_id = c.id) on pv.product_id = p.id) on bpv.product_variant_id = pv.id
    group by pv.id
    having
            amount >= (
            Select  MAX(count)
            from
                (Select p.id, pv.id as pv_di, COUNT(pv.id) as count
                 from baskets_products_variants bpv join baskets b on b.id = bpv.basket_id
                                                    join (variants_product pv join (products p join categories c on p.category_id = c.id) on pv.product_id = p.id) on bpv.product_variant_id = pv.id
                 group by pv.id) as views_sums))

select
    COUNT(*)
from max_views;

-- Выборка из корзин
Select pv.id, p.product_name, pv.title, COUNT(pv.id) as count
from baskets_products_variants bpv join baskets b on b.id = bpv.basket_id
                                   join (variants_product pv join (products p join categories c on p.category_id = c.id) on pv.product_id = p.id) on bpv.product_variant_id = pv.id
group by pv.id;

-- Выборка товаров, просмотренных определённым покупателем
select
    pvw.product_id,
    p.product_name,
    (select pv.price from variants_product pv where pv.product_id = p.id order by pv.id desc limit 1) as product_price,
    pvw.count as general_views_count

from products_views pvw join products p on pvw.product_id = p.id
                        join (visitors v join customers customer on v.id = customer.visitor_id) on pvw.visitor_id = v.id
where customer.id = 11;
select
    count(pvw.id)
from products_views pvw join products p on pvw.product_id = p.id
                        join (visitors v join customers customer on v.id = customer.visitor_id) on pvw.visitor_id = v.id
where customer.id = 10;
