package gp.wagner.backend.repositories.reviews;

import gp.wagner.backend.domain.entites.products.ProductImage;
import gp.wagner.backend.domain.entites.reviews.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewsImagesRepository extends JpaRepository<ReviewImage,Long> {

    //Получение изображений вариантов товара по его id
    List<ReviewImage> findReviewImagesByReviewId(Long reviewId);

    //Получить список объектов по списку id
    Optional<List<ReviewImage>> findReviewImagesByIdIn(List<Long> idList);

    // Добавление изображения к отзыву
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        insert into reviews_images
        (review_id, img_link, img_order)
        values
        (:review_id, :image, :image_order)
    """)
    void insert(@Param("review_id") long reviewId, @Param("image") String image,
                              @Param("image_order") Integer imageOrder);

    //Обновление изображения отзыва
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = """
        update reviews_images set
                img_link = :image,
                img_order = :imageOrder
        where id = :review_image_id
    """)
    void updateProductImage(@Param("review_image_id") long reviewImgId, @Param("image") String image,
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
    Optional<ReviewImage> findReviewImageByImgLinkEquals(String link);

    @Query(value = """
    select
    ri
    from
        ReviewImage ri
    where
        ri.review.id = :review_id and ri.imgOrder = :order
""")
    Optional<ProductImage> findByReviewIdAndImgOrder(@Param("review_id") long productVariantId, @Param("order") int imgOrder);

    // Получит максимальный приоритет для изображений заданного отзыва
    @Query(value = """
    select
        max(ri.imgOrder)
    from
        ReviewImage ri
    where
        ri.review.id = :review_id and ri.imgOrder is not null 
""")
    Integer getMaxImgOrder(@Param("review_id") long reviewId);
}
