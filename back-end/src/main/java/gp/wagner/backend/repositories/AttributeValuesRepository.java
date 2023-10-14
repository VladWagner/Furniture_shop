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

import java.util.ArrayList;
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

}

