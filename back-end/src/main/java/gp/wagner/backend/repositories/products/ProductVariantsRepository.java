package gp.wagner.backend.repositories.products;

import gp.wagner.backend.domain.entities.products.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductVariantsRepository extends JpaRepository<ProductVariant,Long> {

    //Получение неудалённых вариантов товара по его id
    @Query(value = """
     select
     pv
     from ProductVariant pv
     where pv.product.id = :product_id and (pv.isDeleted is null  or pv.isDeleted = false)
""")
    List<ProductVariant> findProductVariantsByProductId(@Param("product_id") Long productId);

    // Получение неудалённых вариантов товаров по id их товаров
    @Query(value = """
     select
     pv
     from ProductVariant pv
     where pv.product.id in :product_id_list and (pv.isDeleted is null  or pv.isDeleted = false)
""")
    List<ProductVariant> findProductVariantsByProductIdList(@Param("product_id_list") List<Long> productIdList);

    //Получение удалённых вариантов товара по его id
    @Query(value = """
     select
     pv
     from ProductVariant pv
     where pv.product.id = :product_id and pv.isDeleted = true
""")
    List<ProductVariant> findDeletedProductVariantsByProductId(@Param("product_id") Long productId);

    // Получение неудалённых вариантов товаров по их id
    @Query(value = """
     select
     pv
     from ProductVariant pv
     where pv.id in :pv_id_list and (pv.isDeleted is null  or pv.isDeleted = false)
""")
    List<ProductVariant> findProductVariantsByIdList(@Param("pv_id_list") List<Long> pvIdsList);

    //Добавление вариантов товара
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        insert into variants_product
        (product_id, preview_img, title, price)
        values
        (:productId, :previewImg, :title, :price)
    """)
    int insertProductVariant(@Param("productId") long productId, @Param("previewImg") String previewImg,
                              @Param("title") String title, @Param("price") int price );

    //Обновление вариантов товара
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        update variants_product set product_id = :productId,
                                    preview_img = :previewImg,
                                    title = :title,
                                    price = :price
        where id = :productVariantId
    """)
    void updateProductVariant(@Param("productVariantId") long productVariantId, @Param("productId") long productId,
                            @Param("previewImg") String previewImg, @Param("title") String title, @Param("price") int price);

    //Обновление thumbnail варианта товара
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        update variants_product set preview_img = :previewImg
        where id = :productVariantId
    """)
    void updateProductVariantPreview(@Param("productVariantId") long productVariantId, @Param("previewImg") String previewImg);


    //((coalesce(:product_id, -1) > 0) and :products_id_list is null and pv.product.id = :product_id) or
    //((coalesce(:product_id, -1) <= 0) and :products_id_list is not null and pv.product.id in :products_id_list) or
    //((coalesce(:product_id, -1) > 0) and :products_id_list is not null and (pv.product.id = :product_id or pv.product.id in :products_id_list))

    @Transactional
    @Modifying
    @Query(
            value = """
        update ProductVariant pv set
            pv.isDeleted = true
        where
            ((:product_id is not null and :product_id > 0) and :products_id_list is null and pv.product.id = :product_id) or
            ((:product_id is null or :product_id <= 0) and :products_id_list is not null and pv.product.id in :products_id_list) or
            ((:product_id is not null and :product_id > 0) and :products_id_list is not null and (pv.product.id = :product_id or pv.product.id in :products_id_list))
    """)
    void deleteVariantsByProduct(@Param("product_id") Long productId, @Param("products_id_list") List<Long> productIdsList);
    @Transactional
    @Modifying
    @Query(
            value = """
        update ProductVariant pv set
            pv.isDeleted = true
        where
            pv.product.id in :products_id_list
    """)
    void deleteVariantsByProductIdList(@Param("products_id_list") List<Long> productIdsList);

    // Восстановить варианты по id/списку id товара
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        update variants_product set
                                    is_deleted = false
        -- where product_id = :product_id
        where
        case
            when (:product_id is not null and :product_id > 0) and :products_id_list is null
                then product_id = :product_id
            when (:product_id is null or :product_id <= 0) and :products_id_list is not null
                then product_id in :products_id_list
            when :product_id is not null and :products_id_list is not null
                then product_id = :product_id or product_id in :products_id_list
            else 1 < 0
         end
    """)
    void recoverVariantsByProduct(@Param("product_id") Long productId, @Param("products_id_list") List<Long> productIdsList);

    @Transactional
    @Modifying
    @Query(
            value = """
        update ProductVariant pv set
            pv.isDeleted = false
        where
            pv.isDeleted = true and pv.product.id in :products_id_list
    """)
    void recoverVariantsByProductIdList(@Param("products_id_list") List<Long> productIdsList);

    //Получение максимального id
    @Query(value = """
    select
        max(pv.id)
    from
        ProductVariant pv
    """)
    long getMaxId();

    // Получить список id вариантов в списке категорий
    @Query(nativeQuery = true,
            value = """
        select
        pv.id
        from variants_product pv join products p on pv.product_id = p.id
        where p.category_id in :categories_ids_list
    """)
    List<Long> getProductsVariantsIdsByCategoriesIdsList(@Param("categories_ids_list") List<Long> categoriesIds);

    // Получить список id вариантов использующих заданную скидку, которые находятся в списке категорий
    @Query(nativeQuery = true,
            value = """
        select
        pv.id
        from variants_product pv join products p on pv.product_id = p.id
        where (pv.discount_id is not null and pv.discount_id = :discount_id) and
         p.category_id in :categories_ids_list
    """)
    List<Long> getProductsVariantsIdsByCategoriesIdsListAndDiscount(@Param("discount_id") long discountId, @Param("categories_ids_list") List<Long> categoriesIds);

    // Получить список id вариантов использующих заданную скидку
    @Query(nativeQuery = true,
            value = """
        select
        pv.id
        from variants_product pv
        where pv.discount_id = :discount_id
    """)
    List<Long> getProductsVariantsIdsWithDiscount(@Param("discount_id") long discountId);

    // Получить список id вариантов использующих заданную скидку, id которых находится в заданном списке
    @Query(nativeQuery = true,
            value = """
        select
        pv.id
        from variants_product pv
        where pv.id in :pv_ids_list and pv.discount_id = :discount_id
    """)
    List<Long> getProductsVariantsIdsWithDiscount(@Param("discount_id") long discountId, @Param("pv_ids_list") List<Long> pvIds);

    // Получить список вариантов использующих заданную скидку, id которых находится в заданном списке
    @Query(value = """
        select
        pv
        from ProductVariant pv
        where pv.id in :pv_ids_list and (pv.discount is not null and pv.discount.id = :discount_id)
    """)
    List<ProductVariant> getProductsVariantsWithDiscountInIdsList(@Param("discount_id") long discountId, @Param("pv_ids_list") List<Long> pvIds);

}
