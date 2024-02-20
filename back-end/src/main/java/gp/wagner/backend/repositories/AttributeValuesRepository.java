package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entites.eav.AttributeValue;
import gp.wagner.backend.domain.entites.products.Product;
import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface AttributeValuesRepository extends JpaRepository<AttributeValue,Long> {

    //Попытка выборки атрибутов и товаров связанных с ними по нескольким полям (по нескольким типам)
    @Query(value = """
    select
    av.product
    from AttributeValue av
    where av.attribute.attributeName in :attrNames and (av.strValue in :values or av.intValue in :values)
    """)
    List<Product> getProductsFromAttributes(@Param("attrNames")List<String> names, @Param("values")List<String> values);

    //Получить все атрибуты определённого товара
    List<AttributeValue> findAttributeValuesByProductId(Long product_id);

    //Добавление значения из Dto
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
    insert into attributes_values
        (product_id, attribute_id, txt_values, int_value, float_value, double_value, bool_value, date_value)
    values
        (:productId,:attributeId,:txtValue,:intValue,:floatValue,:doubleValue,:boolValue,:dateValue)
    """)
    int insertValue(@Param("productId") long productId, @Param("attributeId") long attributeId,
                       @Param("txtValue") @Nullable String txtValue, @Param("intValue") @Nullable Integer intValue,
                       @Param("floatValue") @Nullable Float floatValue, @Param("doubleValue") @Nullable Double showProduct,
                       @Param("boolValue") @Nullable Integer boolValue, @Param("dateValue") @Nullable Date dateValue);

    //Изменение значений характеристик
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
    update attributes_values set txt_values = :txtValue,
                                 int_value = :intValue,
                                 float_value = :floatValue,
                                 double_value = :doubleValue,
                                 bool_value = :boolValue,
                                 date_value = :dateValue
    where id = :attributeValueId
    """)
    int updateValue(@Param("attributeValueId") long attributeValueId,
                       @Param("txtValue") @Nullable String txtValue, @Param("intValue") @Nullable Integer intValue,
                       @Param("floatValue") @Nullable Float floatValue, @Param("doubleValue") @Nullable Double doubleValue,
                       @Param("boolValue") @Nullable Integer boolValue, @Param("dateValue") @Nullable Date dateValue);

    //Удаление характеристик по списку id
    @Transactional
    @Modifying
    void deleteByIdIn(List<Long> ids);

    // Получить значения атрибутов и их диапазоны для фильтрации
    @Query(nativeQuery = true,value = """
        with products_in_category as(
            select
                p.id
            from
                products p
            where
                (:categoryId > 0 and p.category_id = :categoryId and p.is_deleted = false and p.show_product = true)
                    or :categoryId <= 0)
        
        select
            av.attribute_id as attributeId,
            prod_attr.attr_name as attributeName,
            prod_attr.priority,
        
            MIN(av.int_value) as 'min',
            MAX(av.int_value) as 'max',
        
            av.txt_values as 'value'
        
        from
            attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id
        where
             av.product_id in (select pic.id from products_in_category pic) and
             (av.int_value is not null or (av.txt_values is not null and av.txt_values != '')) and
             prod_attr.is_shown is true
        group by
            prod_attr.attr_name, av.attribute_id, av.txt_values;
    """)
    List<Object[]> getAttributeValuesByCategory(@Param("categoryId") long categoryId);

    // Получить значения атрибутов и их диапазоны для фильтрации
    @Query(nativeQuery = true,value = """
        with products_in_category as(
            select
                p.id
            from
                products p
            where
                p.category_id in :category_id_list and p.is_deleted = false and p.show_product = true)
        
        select
            av.attribute_id as attributeId,
            prod_attr.attr_name as attributeName,
            prod_attr.priority,
        
            MIN(av.int_value) as 'min',
            MAX(av.int_value) as 'max',
        
            av.txt_values as 'value'
        
        from
            attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id
        where
             av.product_id in (select pic.id from products_in_category pic) and
             (av.int_value is not null or (av.txt_values is not null and av.txt_values != '')) and
             prod_attr.is_shown is true
        group by
            prod_attr.attr_name, av.attribute_id, av.txt_values;
    """)
    List<Object[]> getAttributeValuesByCategories(@Param("category_id_list") List<Long> categoryId);

    //Получить значения атрибутов и их диапазоны для фильтрации
    @Query(nativeQuery = true,value = """
        with products_in_category as(
            select
                p.id
            from
                products p join variants_product pv on p.id = pv.product_id
                           join producers producer on p.producer_id = producer.id
            where
                (p.product_name like concat('%',:keyword,'%') or
                p.description like concat('%',:keyword,'%') or
                pv.title like concat('%',:keyword,'%') or
                producer.producer_name like concat('%',:keyword,'%')) and
                p.is_deleted = false and p.show_product = true)
        
        select
            av.attribute_id as attributeId,
            prod_attr.attr_name as attributeName,
            prod_attr.priority,
        
            MIN(av.int_value) as 'min',
            MAX(av.int_value) as 'max',
        
            av.txt_values as 'value'
        
        from
            attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id
        where
             av.product_id in (select pic.id from products_in_category pic) and
             (av.int_value is not null or (av.txt_values is not null and av.txt_values != '')) and
             prod_attr.is_shown is true
        group by
            prod_attr.attr_name, av.attribute_id, av.txt_values;
    """)
    List<Object[]> getAttributeValuesByKeyword(@Param("keyword") String key);

    // Получить значения атрибутов по id самого атрибута
    @Query(nativeQuery = true,value = """ 
        select
            av.txt_values as 'str_value',
            av.int_value as 'int_value'
        from
            attributes_values av join products_attributes prod_attr on av.attribute_id = prod_attr.id
        where
             av.product_id = :prod_attr_id and
             (av.int_value is not null or (av.txt_values is not null and av.txt_values != ''))
        group by
            av.txt_values, av.int_value;
    """)
    List<Object[]> getAttributeValuesByAttrId(@Param("prod_attr_id") long attrId);

}

