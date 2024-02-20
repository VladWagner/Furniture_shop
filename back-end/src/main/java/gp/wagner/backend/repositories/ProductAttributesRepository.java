package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entites.eav.ProductAttribute;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductAttributesRepository extends JpaRepository<ProductAttribute,Long> {

    // Добавление значения из Dto
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
    insert into products_attributes
        (attr_name, priority, is_shown)
    values
        (:attr_name, :priority, :is_shown)
    """)
    void insert(@Param("attr_name") long productId, @Param("priority") float priority, @Param("is_shown") float isShow);

    // Изменение значений характеристик
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
    update products_attributes set
            attr_name = :attr_name,
            priority = :priority,
            is_shown = :is_shown
    where id = :pa_id
    """)
    void update(@Param("pa_id") long productAttributeId, @Param("attr_name") String attrName,
                @Param("priority") float priority, @Param("is_shown") float isShow);


    // Изменить приоритет атрибута
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
    update products_attributes pa set
        pa.priority = :priority
    where id = :id
    """)
    void updatePriority(@Param("id") long id, @Param("priority") float priority);

    // Получить атрибуты по id категории
    @Query(nativeQuery = true, value = """
    select
        pa.id, pa.attr_name, pa.priority, pa.is_shown
    from
        attributes_categories ac join products_attributes pa on pa.id = ac.attribute_id
    where ac.category_id = :category_id
    order by pa.priority
    """)
    List<Tuple> getProductAttributesByCategoryId(@Param("category_id") int categoryId);

    // Скрыть несколько атрибутов по списку id
    @Transactional
    @Modifying
    @Query(value = """
    update ProductAttribute pa set
        pa.isShown = :is_shown
    where pa.id in :id_list
    """)
    void changeDisplayStateByIdsList(@Param("id_list") List<Long> ids, @Param("is_shown") boolean isShown);

    // Найти атрибуты по списку id
    @Query(value = """
    select
        pa
    from ProductAttribute pa
    where pa.id in :id_list
    """)
    Optional<List<ProductAttribute>> getProductAttributesByIdsList(@Param("id_list") List<Long> ids);


    @Query(nativeQuery = true, value = """
    select
        max(pa.priority)
    from
        products_attributes pa
    """)
    float getMaxPriority();

    // Удалить записи из таблицы многие ко многим
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
    delete
    from
        attributes_categories ac
    where ac.attribute_id = :pa_id and ac.category_id in :categories_id_list
    """)
    void deleteFromAttributesCategoriesByIdList(@Param("pa_id") long productAttrId, @Param("categories_id_list") List<Long> ids);

}

