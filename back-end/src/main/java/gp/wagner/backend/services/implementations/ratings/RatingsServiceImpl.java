package gp.wagner.backend.services.implementations.ratings;

import gp.wagner.backend.domain.dto.request.crud.ratings.RatingRequestDto;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.ratings.Rating;
import gp.wagner.backend.domain.entites.ratings.RatingStatistics;
import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.SortingUtils;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.RatingsSortEnum;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.ratings.RatingsRepository;
import gp.wagner.backend.repositories.ratings.RatingsStatisticsRepository;
import gp.wagner.backend.services.interfaces.ratings.RatingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RatingsServiceImpl implements RatingsService {

    private RatingsRepository ratingsRepository;

    @Autowired
    public void setRatingsRepository(RatingsRepository repository) {
        this.ratingsRepository = repository;
    }

    private RatingsStatisticsRepository ratingsStatRepository;

    @Autowired
    public void setRatingsStatisticsRepository(RatingsStatisticsRepository repository) {
        this.ratingsStatRepository = repository;
    }


    @Override
    public Page<Rating> getAll(int pageNum, int limit,
                               RatingsSortEnum sortEnum, GeneralSortEnum sortType) {

        if (pageNum > 0)
            pageNum -= 1;

        Pageable pageable = PageRequest.of(pageNum, limit, SortingUtils.createSortForRatingsSelection(sortEnum, sortType));

        return ratingsRepository.findAll(pageable);
    }

    @Override
    public Rating getById(Long id) {
        if (id == null || id <= 0)
            throw new ApiException("Id оценки задан некорректно!");

        return ratingsRepository.findById(id).orElseThrow(() -> new ApiException(String.format("Оценка с id %d не найдена!", id)));
    }
    @Override
    public Rating getByProductAndUserId(long productId, long userId) {
        if (productId <= 0 || userId <= 0)
            throw new ApiException("Не вышло получить оценку по id товара и пользователя. Параметры заданы некорректно!");

        return ratingsRepository.findByProductAndUserId(productId, userId).orElse(null);
    }

    @Override
    public long create(Rating rating) {
        if (rating == null)
            throw new ApiException("Не удалось создать запись оценки товара. Параметр задан некорректно!");

        return ratingsRepository.saveAndFlush(rating).getId();
    }

    @Override
    public Rating createOrUpdate(RatingRequestDto dto) {
        if (dto == null || dto.getProductId() == null || dto.getUserId() == null || dto.getRating() == null)
            throw new ApiException("Не получилось создать оценку для товара . Dto задан некорректно!");

        // Товар для которого устанавливается оценка
        Product product = Services.productsService.getById(dto.getProductId().longValue());

        User user = Services.usersService.getById(dto.getUserId());

        Rating rating = dto.getId() != null ? ratingsRepository.findById(dto.getId()).orElse(null) :
                ratingsRepository.findByProductAndUserId(product.getId(), user.getId()).orElse(null);

        // Если заданная в DTO оценка совпадает с найденной, тогда ничего не меняем
        if (rating != null && rating.getRating().equals(dto.getRating()))
            return rating;

        Integer oldRatingValue = null;

        // Если оценка не была найдена
        if (rating == null){
            rating = new Rating(dto.getRating(), user, product);
        }
        else {
            oldRatingValue = rating.getRating();
            rating.setRating(dto.getRating());
        }

        ratingsRepository.saveAndFlush(rating);

        RatingStatistics statistics = ratingsStatRepository.findRatingStatisticsByProductId(product.getId()).orElse(null);

        // Изменить значения в статистике оценок товара
        if (statistics != null){
            int amount = statistics.getAmount();
            int newSum;

            // Если старое значение задано
            if (oldRatingValue != null)
                // Ср.значение оценки * кол-во оценок - старое значение оценки + новое значение оценки для того же кол-ва
                newSum = (Math.round((statistics.getAvg() * amount)) - oldRatingValue) + rating.getRating();
            // Задана новая оценка
            else{
                newSum = Math.round((statistics.getAvg() * amount)) + rating.getRating();
                statistics.setAmount(amount+1);
            }

            // Пересчитать среднюю оценку
            statistics.setAvg((float) newSum/statistics.getAmount());

        }
        else
            statistics = new RatingStatistics(product, rating.getRating().floatValue(), 1);

        ratingsStatRepository.saveAndFlush(statistics);

        return rating;
    }

    @Override
    public void removeRating(long productId, long userId) {
        Product product = Services.productsService.getById(productId);
        User user = Services.usersService.getById(userId);

        Rating rating = ratingsRepository.findByProductAndUserId(product.getId(), user.getId()).orElse(null);

        if (rating == null)
            throw new ApiException(String.format("Оценка установленная для товара с id: %d пользователем с id: %d не найдена!",
                    product.getId(), user.getId()));

        // Найти статистику оценок для заданного товара
        RatingStatistics statistics = ratingsStatRepository.findRatingStatisticsByProductId(product.getId()).orElse(null);

        if (statistics == null) {
            ratingsRepository.delete(rating);
            return;
        }

        // Убрать из общей суммы текущую оценку
        int sum = Math.round(statistics.getAvg() * statistics.getAmount()) - rating.getRating();

        statistics.setAmount(sum > 0 ? statistics.getAmount() - 1 : 0);
        statistics.setAvg(sum > 0 ? (float) sum/statistics.getAmount() : 0f);

        ratingsRepository.delete(rating);
        ratingsStatRepository.saveAndFlush(statistics);

    }

    @Override
    public void update(long ratingId, int newRating, int productId, long userId) {

        ratingsRepository.update(ratingId, newRating, productId, userId);
    }

    @Override
    public void update(Rating rating) {
        if (rating == null)
            throw new ApiException("Не удалось обновить оценку! Параметр равен null");

        ratingsRepository.saveAndFlush(rating);
    }

}
