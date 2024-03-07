package gp.wagner.backend.repositories.reviews;

import gp.wagner.backend.domain.entites.reviews.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ReviewsRepository extends JpaRepository<Review,Long> {

    // Добавление отзыва на товар
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert into reviews
        (product_id, user_id, 'text')
        values
        (:product, :user, :text)
    """)
    int insert(@Param("product") int productId, @Param("user") long userId, @Param("text") String text);

    // Изменение отзыва на товар
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
    update reviews set
                        product_id = :product,
                        user_id = :user,
                        reviews.text = :text
    where id = :review_id
    """)
    void update(@Param("review_id") long reviewId ,@Param("product") int productId, @Param("user") long userId, @Param("text") String text);

    // Удалить отзыв
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
    update reviews set
                        deleted_at = now()
    where id = :review_id
    """)
    void deleteSoft(@Param("review_id") long reviewId);

    //Получить maxId
    @Query(value = """
    select
        max(pv.id)
    from
        ProductViews pv
    """)
    long getMaxId();

    // Получит отзывы для товара
    @Query(value = """
    select
    r
    from Review r
    where r.product.id = :product_id and ((:verified is not null and r.isVerified = :verified) or :verified is null)
""")
    Page<Review> getReviewByProductId(@Param("product_id") long productId, @Param("verified") Boolean verified, Pageable pageable);
    //Page<Review> getReviewByProductId(long productId, Pageable pageable);
}
