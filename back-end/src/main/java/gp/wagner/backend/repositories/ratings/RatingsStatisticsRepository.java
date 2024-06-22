package gp.wagner.backend.repositories.ratings;

import gp.wagner.backend.domain.entities.ratings.RatingStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RatingsStatisticsRepository extends JpaRepository<RatingStatistics,Long> {

    // Добавление статистики оценки
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert into ratings_statistics
        (product_id, ratings_sum, avg, amount)
        values
        (:product, :sum, :avg, :amount)
    """)
    int insert(@Param("product") int productId, @Param("sum") int ratingsSum, @Param("avg") float avgRating,  @Param("amount") int ratingsAmount);

    // Изменение статистики оценки
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
    update ratings_statistics set
                        product_id = :product,
                        ratings_sum = :sum,
                        avg = :avg,
                        amount = :amount
    where id = :rating_stat_id
    """)
    void update(@Param("rating_stat_id") long ratingStatisticsId, @Param("product") int productId, @Param("sum") int ratingsSum, @Param("avg") float avgRating, @Param("amount") int ratingsAmount);

    //Получить maxId
    @Query(value = """
    select
        max(pv.id)
    from
        ProductViews pv
    """)
    long getMaxId();

    Optional<RatingStatistics> findRatingStatisticsByProductId(long productId);
}
