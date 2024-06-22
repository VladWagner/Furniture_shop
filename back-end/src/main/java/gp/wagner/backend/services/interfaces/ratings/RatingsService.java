package gp.wagner.backend.services.interfaces.ratings;

import gp.wagner.backend.domain.dto.request.crud.ratings.RatingRequestDto;
import gp.wagner.backend.domain.entities.ratings.Rating;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.RatingsSortEnum;
import org.springframework.data.domain.Page;


public interface RatingsService {


    // Выборка всех записей
    Page<Rating> getAll(int pageNum, int limit,
                        RatingsSortEnum sortEnum, GeneralSortEnum sortType);

    //  Выборка записи под id
    Rating getById(Long id);

    Rating getByProductAndUserId(long productId, long userId);


    // Добавление записи
    long create(Rating rating);
    Rating createOrUpdate(RatingRequestDto ratingDto);


    // Изменение записи
    void update(long ratingId, int newRating, int productId, long userId);
    void update(Rating rating);
    void removeRating(long productId, long userId);

}
