package gp.wagner.backend.services.implementations.reviews;

import gp.wagner.backend.domain.dto.request.crud.reviews.ReviewRequestDto;
import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.entites.products.Discount;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.ratings.Rating;
import gp.wagner.backend.domain.entites.reviews.Review;
import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.domain.exceptions.suppliers.ReviewNotFound;
import gp.wagner.backend.infrastructure.SortingUtils;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.ReviewsSortEnum;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.ratings.RatingsRepository;
import gp.wagner.backend.repositories.reviews.ReviewsRepository;
import gp.wagner.backend.services.interfaces.reviews.ReviewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ReviewsServiceImpl implements ReviewsService {

    private ReviewsRepository reviewsRepository;

    @Autowired
    public void setReviewsRepository(ReviewsRepository repository) {
        this.reviewsRepository = repository;
    }

    @Override
    public Page<Review> getAll(int pageNum, int limit,
                               ReviewsSortEnum sortEnum, GeneralSortEnum sortType) {
        if (pageNum > 0)
            pageNum -= 1;

        Pageable pageable = PageRequest.of(pageNum, limit, SortingUtils.createSortForReviewsSelection(sortEnum, sortType));

        return reviewsRepository.findAll(pageable);
    }

    @Override
    public Review getById(Long id) {
        if (id == null || id <= 0)
            throw new ApiException("Id отзыва задан некорректно!");

        return reviewsRepository.findById(id).orElseThrow(new ReviewNotFound(id, null, null));

    }

    @Override
    public Page<Review> getByProductId(long productId, Boolean verified, int pageNum, int limit,
                                       ReviewsSortEnum sortEnum, GeneralSortEnum sortType) {

        if (pageNum > 0)
            pageNum -= 1;

        Pageable pageable = PageRequest.of(pageNum, limit, SortingUtils.createSortForReviewsSelection(sortEnum, sortType));

        return reviewsRepository.getReviewByProductId(productId, verified, pageable);
    }

    @Override
    public long create(Review review) {
        if (review == null)
            throw new ApiException("Не получилось создать отзыв для товара!");
        return reviewsRepository.saveAndFlush(review).getId();
    }

    @Override
    public Review create(ReviewRequestDto dto) {
        if (dto == null || dto.getProductId() == null || dto.getUserId() == null || dto.getOrderCode() == null)
            throw new ApiException("Не получилось создать отзыв для товара . Dto задан некорректно!");

        Product product = Services.productsService.getById(dto.getProductId());
        User user = Services.usersService.getById(dto.getUserId());


        Order order = Services.ordersService.getByOrderCode(dto.getOrderCode());

        // Содержит ли список заказанных товаров тот, на который задаётся отзыв
        boolean orderContainsProduct = order != null && order.getOrderAndPVList()
                .stream()
                .anyMatch(opv -> opv.getProductVariant().getProduct().getId().equals(product.getId()));

        // Если заказа с таким номером нет или в нём не найден заданный товар
        if (order == null || !orderContainsProduct)
            throw new ApiException(String.format("Задать отзыв на товар с id: %d не вышло. " +
                    "Заказ с номером %d либо не существует, либо не содержит заданный товар!", product.getId(), dto.getOrderCode()));

        Review review = new Review(dto.getText(), user, product, false);

        return reviewsRepository.saveAndFlush(review);
    }

    @Override
    public Review update(ReviewRequestDto dto) {
        if (dto == null || dto.getId() == null)
            throw new ApiException("Не удалось изменить отзыв. Dto задан некорректно!");

        Review review = reviewsRepository.findById(dto.getId()).orElseThrow(new ReviewNotFound(dto.getId(), null, null));

        if (dto.getText().isBlank())
            return review;

        review.setText(dto.getText());
        return reviewsRepository.saveAndFlush(review);
    }
    @Override
    public boolean verifyReview(long reviewId) {
        if (reviewId <= 0)
            throw new ApiException("id отзыва задан некорректно!");

        Review review = reviewsRepository.findById(reviewId)
                .orElseThrow(new ReviewNotFound(reviewId, null, null));

        review.setIsVerified(true);

        return reviewsRepository.saveAndFlush(review).getIsVerified();
    }

    @Override
    public void deleteReview(long reviewId) {
        if (reviewId <= 0)
            throw new ApiException("Id отзыва задан некорректно!");

        Review review = reviewsRepository.findById(reviewId)
                .orElseThrow(new ReviewNotFound(reviewId, null, null));

        review.setDeletedAt(new Date());
    }


    @Override
    public void update(Review review) {
        if (review == null)
            throw new ApiException("Не удалось обновить оценку! Параметр равен null");

        reviewsRepository.saveAndFlush(review);
    }

}
