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
    -- Ввыборка атрибутов по заданной основной категории
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
   /* vp.id as product_id,*/
   /* vp.producer_name,*/
   /* vp.category,*/
   /* vp.parent_category,*/
   /* pa.attr_name,*/
    attributes_values.txt_values,
    attributes_values.int_value,
    attributes_values.float_value,
    count(*)
from
    attributes_values join view_products vp on attributes_values.product_id = vp.id
                      join products_attributes pa on attributes_values.attribute_id = pa.id
where
    vp.category like CONCAT('%', @category,'%') and pa.attr_name like 'материалы'
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
set @categoryId = 4;
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




