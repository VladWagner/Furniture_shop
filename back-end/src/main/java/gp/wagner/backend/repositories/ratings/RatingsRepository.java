package gp.wagner.backend.repositories.ratings;

import gp.wagner.backend.domain.entities.ratings.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RatingsRepository extends JpaRepository<Rating,Long> {

    // Добавление оценки
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert into ratings
        (rating, product_id, user_id)
        values
        (:rating, :product, :user)
    """)
    int insert(@Param("rating") int rating, @Param("product") int productId, @Param("user") long userId);

    // Изменение оценки
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
    update ratings set
                        rating = :visitor,
                        product_id = :product,
                        user_id = :user
    where id = :rating_id
    """)
    void update(@Param("rating_id") long ratingId ,@Param("rating") int newRating, @Param("product") int productId, @Param("user") long userId);

    //Получить maxId
    @Query(value = """
    select
        max(pv.id)
    from
        ProductViews pv
    """)
    long getMaxId();

    Optional<Rating> findByProductId(Long id);

    @Query(value = """
    select
        r
    from Rating r
    where r.product.id = :product_id and r.user.id = :user_id
""")
    Optional<Rating> findByProductAndUserId(@Param("product_id") Long productId, @Param("user_id") Long userId);
}
