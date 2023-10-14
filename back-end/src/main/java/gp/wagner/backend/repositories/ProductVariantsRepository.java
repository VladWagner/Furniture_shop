package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entites.categories.Category;
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

    //Получение вариантов товара по его id
    @Query(value = """
     select
     pv
     from ProductVariant pv
     where pv.product.id = :product_id
""")
    List<ProductVariant> findProductVariantsByProductId(@Param("product_id") Long productId);

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

    //Получение максимального id
    @Query(value = """
    select
        max(pv.id)
    from
        ProductVariant pv
    """)
    long getMaxId();

}
