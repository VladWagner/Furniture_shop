package gp.wagner.backend.services.interfaces.reviews;

import gp.wagner.backend.domain.dto.request.crud.reviews.ReviewRequestDto;
import gp.wagner.backend.domain.entities.reviews.Review;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.ReviewsSortEnum;
import org.springframework.data.domain.Page;


public interface ReviewsService {

    // Выборка всех отзывов на все товары
    Page<Review> getAll(int pageNum, int limit,
                        ReviewsSortEnum sortEnum, GeneralSortEnum sortType);

    // Выборка записи под id
    Review getById(Long id);

    // Выборка отзыва под id товара
    Page<Review> getByProductId(long productId, Boolean verified, int pageNum, int limit,
                                ReviewsSortEnum sortEnum, GeneralSortEnum sortType);

    // Добавление записи
    long create(Review review);
    Review create(ReviewRequestDto reviewDto);

    // Подтверждение корректности отзыва
    boolean verifyReview(long reviewId);

    // Удаление отзыва
    void deleteReview(long reviewId);


    // Изменение записи
    void update(Review review);
    Review update(ReviewRequestDto reviewDto);

}
