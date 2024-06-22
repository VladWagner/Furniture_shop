package gp.wagner.backend.repositories.orders;

import gp.wagner.backend.domain.entities.orders.OrderAndProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OrdersAndProductVariantsRepository extends JpaRepository<OrderAndProductVariant,Long> {

    //Добавление записи справочника для заказа
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert orders_products_variants
        (product_variant_id, order_id, products_count)
        values
            (:product_variant, :order, :count)
    """)
    void insertOrderAndProductVariant(@Param("product_variant") int productVariantId, @Param("order") long orderId, @Param("count") int productsCount);

    //Изменение справочника заказа
    @Transactional
    @Query(nativeQuery = true,
    value = """
    update orders_products_variants set
              product_variant_id = :product_variant,
              order_id = :order_id,
              products_count = :products_count
   where id = :id
    """)
    void updateOrderAndProductVariant(@Param("id") long id, @Param("product_variant") int productVariantId, @Param("order_id") long orderId, @Param("products_count") int productsCount);

    //Получить значения по id заказа
    List<OrderAndProductVariant> findOrderAndProductVariantsByOrder_Id(long orderId);

    //Получить значения по определённому варианту товара
    Page<OrderAndProductVariant> findOrderAndProductVariantsByProductVariantId(long productVariantId, Pageable pageable);

    // Получить записи по id или номеру заказа
    @Query(value = """
    Select
        opv
    from OrderAndProductVariant opv
    where
            CASE
                WHEN (:id IS NULL OR :id <= 0) AND (:code IS NOT NULL AND :code > 0) THEN (opv.order.code = :code)
                ELSE (opv.order.id = :id)
            END
    """)
    List<OrderAndProductVariant> findOrderAndPvByIdOrCode(@Param("id") Long id, @Param("code") Long orderCode);

    // Получить записи по id или номеру заказа
    @Query(value = """
    Select
        opv
    from OrderAndProductVariant opv
    where
            opv.order.id in :ids_list
    """)
    List<OrderAndProductVariant> findOrdersAndPvByIdListOrCodesList(@Param("ids_list") List<Long> ids);

    //Получить maxId
    @Query(value = """
    select
        max(opv.id)
    from
        OrderAndProductVariant opv
    """)
    long getMaxId();


}
