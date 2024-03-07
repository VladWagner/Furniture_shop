package gp.wagner.backend.services.interfaces.reviews;

import gp.wagner.backend.domain.entites.reviews.Review;
import gp.wagner.backend.domain.entites.reviews.ReviewImage;

import java.util.List;
import java.util.Optional;


public interface ReviewsImagesService {

    //Добавление записи
    void create(ReviewImage reviewImage);

    void create(long reviewId, String imgLink,int order );
    void createAll(List<String> filesUris, Review review);
    void addAll(List<String> filesUris, Review review);

    //Изменение записи
    void update(ReviewImage reviewImage);
    void update(Long imageId, String imageLink, Integer imageOrder);

    //Удаление изображения по id
    void deleteById(long reviewImageId);
    void deleteAll(List<ReviewImage> reviewImages);

    //Выборка всех записей
    List<ReviewImage> getAll();

    //Выборка записи по id
    ReviewImage getById(Long id);

    //Выборка записи по ссылке
    ReviewImage getByLink(String link);

    //Выборка записи по id отзыва и порядковому номеру
    Optional<ReviewImage> getByReviewIdAndByOrder(long reviewId, int imgOrder);

    //Выборка записи по списку id
    List<ReviewImage> getByIdList(List<Long> idList);

    //Выборка записи под id варианта исполнения товара
    List<ReviewImage> getByReviewId(Long reviewId);


}
