drop view if exists View_products;
drop view if exists View_categories;
drop view if exists View_products_features;

-- Представление категорий
Create view
    View_categories
as
select
    categories.id,
    ifnull(categories.category_name, '---')     as name,
    ifnull(subcategories.sub_name, '---') as subcategory_name,
    parent_category.category_name  as parent_category,
    categories.parent_id,
    subcategories.id as subcategory_id

from
    -- Присоединять subcategories нужно ко всем categories, поскольку в categories может не быть записей subcategories
    categories left join subcategories on categories.subcategory_id = subcategories.id
               join categories as parent_category on categories.parent_id = parent_category.id;

-- Представление товаров
Create view
    View_products
    as
select
    products.id,
    product_name,
    description,
    ifnull(categories.category_name,s.sub_name) as category,
    (select cat.category_name from categories cat where cat.id = categories.parent_id) as parent_category,
    producers.producer_name,
    if(is_available=1,'true','false') as is_available,
    if(show_product=1,'true','false') as show_product
from products join (categories left join subcategories s on categories.subcategory_id = s.id)
                   on products.category_id = categories.id
              join producers on products.producer_id = producers.id;


-- Представление товаров с характеристиками
Create view
    View_products_features
    as
select
    products.id as product_id,
    product_name,
    description,
    ifnull(categories.category_name,s.sub_name) as category,
    (select cat.category_name from categories cat where cat.id = categories.parent_id) as parent_category,
    producers.producer_name,
    if(is_available=1,'true','false') as is_available,
    if(show_product=1,'true','false') as show_product,
    p_attr.attr_name as feature,
    ifnull(av.txt_values,
        ifnull(av.int_value,
            ifnull(av.float_value,
                ifnull(av.double_value,
                    ifnull(av.bool_value,av.date_value))))) as feature_value

from products join (categories left join subcategories s on categories.subcategory_id = s.id)
                   on products.category_id = categories.id
              join producers on products.producer_id = producers.id
              join (attributes_values av join products_attributes p_attr on av.attribute_id = p_attr.id) on av.product_id = products.id

