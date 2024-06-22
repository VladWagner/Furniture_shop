package gp.wagner.backend.repositories.baskets;

import gp.wagner.backend.domain.entities.baskets.BasketAndProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BasketsAndProductVariantsRepository extends JpaRepository<BasketAndProductVariant,Long> {

    //Добавление записи справочника для заказа
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert baskets_products_variants
               (product_variant_id, basket_id, products_count)
        values
            (:product_variant, :basket, :count)
    """)
    void insertBasketAndProductVariant(@Param("product_variant") int productVariantId, @Param("basket") long orderId, @Param("count") int productsCount);

    //Изменение справочника корзины
    @Transactional
    @Query(nativeQuery = true,
    value = """
    update baskets_products_variants set
              product_variant_id = :product_variant,
              basket_id = :basket_id,
              products_count = :products_count
   where id = :id
    """)
    void updateOrderAndProductVariant(@Param("id") long id, @Param("product_variant") int productVariantId, @Param("basket_id") long basketId, @Param("products_count") int productsCount);

    //Получить значения по id корзины
    List<BasketAndProductVariant> findBasketAndProductVariantsByBasketId(long basketId);


    //Получить значения по списку id корзин
    //Получить maxId
    @Query(value = """
    select
        bpv
    from
        BasketAndProductVariant bpv
    where bpv.basket.id in :baskets_id_list
    """)
    List<BasketAndProductVariant> findBasketAndProductVariantsByBasketIdsList(@Param("baskets_id_list") List<Long> basketIdList);

    //Получить значения по определённому варианту товара
    List<BasketAndProductVariant> findBasketAndProductVariantsByProductVariantId(long productVariantId);

    //Получить maxId
    @Query(value = """
    select
        max(bpv.id)
    from
        BasketAndProductVariant bpv
    """)
    long getMaxId();

    @Transactional
    void deleteBasketAndProductVariantsByBasketId(long basketId);

}
