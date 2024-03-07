package gp.wagner.backend.services.implementations.reviews;

import gp.wagner.backend.domain.entites.reviews.Review;
import gp.wagner.backend.domain.entites.reviews.ReviewImage;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.repositories.reviews.ReviewsImagesRepository;
import gp.wagner.backend.repositories.reviews.ReviewsRepository;
import gp.wagner.backend.services.interfaces.reviews.ReviewsImagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewsImagesServiceImpl implements ReviewsImagesService {

    private ReviewsImagesRepository imagesRepository;

    @Autowired
    public void setReviewsRepository(ReviewsImagesRepository repository) {
        this.imagesRepository = repository;
    }

    @Override
    public void create(ReviewImage reviewImage) {
        if (reviewImage == null)
            throw new ApiException("Не получилось создать изображение для отзыва. Параметр задан некорректно!");

        imagesRepository.saveAndFlush(reviewImage);
    }

    @Override
    public void create(long reviewId, String imgLink, int order) {

        if (reviewId <= 0)
            return;

        imagesRepository.insert(reviewId, imgLink, order);

    }

    @Override
    public void createAll(List<String> filesUris, Review review) {
        List<ReviewImage> reviewImages = new ArrayList<>();

        for (int i = 0; i < filesUris.size(); i++) {
            reviewImages.add(new ReviewImage(review, filesUris.get(i), i+1));
        }

        imagesRepository.saveAllAndFlush(reviewImages);
    }

    @Override
    public void addAll(List<String> filesUris, Review review) {
        List<ReviewImage> reviewImages = new ArrayList<>();

        Integer maxOrder = imagesRepository.getMaxImgOrder(review.getId());

        // Если не удалось найти максимальное значение порядка вывода
        if (maxOrder == null)
            maxOrder = 0;

        for (String uris : filesUris) {
            maxOrder++;
            reviewImages.add(new ReviewImage(review, uris, maxOrder));
        }

        imagesRepository.saveAllAndFlush(reviewImages);
    }

    @Override
    public void update(ReviewImage reviewImage) {

    }

    @Override
    public void update(Long imageId, String imageLink, Integer imageOrder) {

    }

    @Override
    public void deleteById(long reviewImageId) {
        if (reviewImageId <= 0)
            throw new ApiException("Не получилось удалить изображение для отзыва. Параметр задан некорректно!");

        imagesRepository.deleteById(reviewImageId);
    }

    @Override
    public void deleteAll(List<ReviewImage> reviewImages) {
        if (reviewImages == null || reviewImages.isEmpty())
            return;

        imagesRepository.deleteAll(reviewImages);
    }

    @Override
    public List<ReviewImage> getAll() {
        return null;
    }

    @Override
    public ReviewImage getById(Long id) {
        return null;
    }

    @Override
    public ReviewImage getByLink(String link) {
        return null;
    }

    @Override
    public Optional<ReviewImage> getByReviewIdAndByOrder(long reviewId, int imgOrder) {
        return Optional.empty();
    }

    @Override
    public List<ReviewImage> getByIdList(List<Long> idList) {
        if (idList == null || idList.isEmpty())
            throw new ApiException("Не вышло получить изображения отзыва по списку их id. Список идентификатор задан некорректно!");
        return imagesRepository.findReviewImagesByIdIn(idList).orElse(new ArrayList<>());
    }

    @Override
    public List<ReviewImage> getByReviewId(Long reviewId) {
        return null;
    }
}
