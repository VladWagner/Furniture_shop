package gp.wagner.backend.repositories.products;

import gp.wagner.backend.domain.entites.products.ProductVariant;
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

    //Получение неудалённых вариантов товаров по их id
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

    //Удалить варианты по id товара или по списку id-шников
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        update variants_product set
                                    is_deleted = true
        -- where product_id = :product_id
        -- where if(:product_id is not null and :products_id_list is null, product_id = :product_id,
        -- :product_id is null and :products_id_list is not null, product_id in :products_id_list,
        -- :product_id is not null and :products_id_list is not null, product_id = :product_id and product_id in :products_id_list)
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
    void deleteVariantsByProduct(@Param("product_id") Long productId, @Param("products_id_list") List<Long> productIdsList);

    //Восстановить варианты по id товара
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

    //Получение максимального id
    @Query(value = """
    select
        max(pv.id)
    from
        ProductVariant pv
    """)
    long getMaxId();

}
