package gp.wagner.backend.repositories.products;

import gp.wagner.backend.domain.entities.products.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImagesRepository extends JpaRepository<ProductImage,Long> {

    //Получение изображений вариантов товара по его id
    List<ProductImage> findProductImagesByProductVariantId(Long productVariantId);

    //Получить список объектов по списку id
    List<ProductImage> findProductImagesByIdIn(List<Long> idList);

    //Добавление изображений вариантов товара через native query
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        insert into products_images
        (product_variant_id, img_link, img_order)
        values
        (:productVariantId, :image, :imageOrder)
    """)
    void insertProductImage(@Param("productVariantId") long productVariantId, @Param("image") String image,
                              @Param("imageOrder") Integer imageOrder);

    //Обновление вариантов товара через native query
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        update products_images set img_link = :image, img_order = :imageOrder
        where id = :productImageId
    """)
    void updateProductImage(@Param("productImageId") long productImageId, @Param("image") String image,
                              @Param("imageOrder") Integer imageOrder);

    //Получение максимального id
    @Query(value = """
    select
        max(pi.id)
    from
        ProductImage pi
    """)
    long getMaxId();

    //Найти по наименованию
    Optional<ProductImage> findProductImageByImgLinkEquals(String link);

    //Найти по id варианта и порядковому номеру
    Optional<ProductImage> findProductImageByProductVariantIdAndImgOrderIs(long productVariantId, int imgOrder);

    @Query(value = """
    select
    pi
    from 
        ProductImage pi
    where 
        pi.productVariant.id = :pv_id and pi.imgOrder = :order
""")
    Optional<ProductImage> findByVariantIdAndImgOrder(@Param("pv_id") long productVariantId, @Param("order") int imgOrder);
}
