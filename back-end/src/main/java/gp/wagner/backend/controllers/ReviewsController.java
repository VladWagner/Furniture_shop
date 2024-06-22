package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.reviews.ReviewImageDtoContainer;
import gp.wagner.backend.domain.dto.request.crud.reviews.ReviewRequestDto;
import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.dto.response.reviews.ReviewRespDto;
import gp.wagner.backend.domain.entities.reviews.Review;
import gp.wagner.backend.domain.entities.reviews.ReviewImage;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.ReviewsSortEnum;
import gp.wagner.backend.middleware.Services;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/products_reviews")
public class ReviewsController {

    // Задать отзыв на товар
    @PostMapping(value = "/add_review")
    public ResponseEntity<ReviewRespDto> addReview(@Valid @RequestPart(value = "review") ReviewRequestDto reviewDto,
                                                   @RequestPart(value = "files", required = false) List<MultipartFile> files) throws Exception {

        Review createdReview = Services.reviewsService.create(reviewDto);

        if (files == null)
            return ResponseEntity.ok(new ReviewRespDto(createdReview));

        List<String> filesUris = new ArrayList<>();

        //Загрузка файлов
        for (MultipartFile file:files) {

            //Метод заменяет \ на / и убирает .. в заданном пути
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            //Добавить путь файла в список uri, который будет писаться в БД
            filesUris.add(
                    Utils.cleanUrl(
                            Services.fileManageService.saveReviewImg(fileName, file, createdReview).toString()
                    )
            );
        }

        if (!filesUris.isEmpty()) {
            Services.reviewsImagesService.createAll(filesUris, createdReview);
            createdReview = Services.reviewsService.getById(createdReview.getId());
        }

        return ResponseEntity.ok(new ReviewRespDto(createdReview));
    }

    // Изменить отзыв на товар
    @PutMapping(value = "/update_review")
    public ResponseEntity<ReviewRespDto> updateReview(@Valid @RequestPart(value = "review") ReviewRequestDto reviewDto,
                                                      @RequestPart(value = "images_container", required = false) ReviewImageDtoContainer container,
                                                      @RequestPart(value = "files", required = false) List<MultipartFile> files) throws Exception {

        Review updatedReview = Services.reviewsService.update(reviewDto);

        List<String> filesUris = new ArrayList<>();

        //Загрузка файлов

        if (files != null) {
            for (MultipartFile file:files) {

                //Метод заменяет \ на / и убирает .. в заданном пути
                String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

                //Добавить путь файла в список uri, который будет писаться в БД
                filesUris.add(
                        Utils.cleanUrl(
                                Services.fileManageService.saveReviewImg(fileName, file, updatedReview).toString()
                        )
                );
            }
        }

        // Удаление изображений
        if (container.deletedImagesId() != null){
            List<ReviewImage> deletingImages = Services.reviewsImagesService.getByIdList(container.deletedImagesId());

            for (ReviewImage image : deletingImages) {
                Services.fileManageService.deleteFile(new URI(image.getImgLink()));
            }

            // Удалить все выбранные изображения за раз
            Services.reviewsImagesService.deleteAll(deletingImages);
        }

        if (!filesUris.isEmpty())
            Services.reviewsImagesService.addAll(filesUris, updatedReview);

        updatedReview = Services.reviewsService.getById(updatedReview.getId());

        return ResponseEntity.ok(new ReviewRespDto(updatedReview));
    }

    @PutMapping(value = "/verify/{review_id}")
    public ResponseEntity<String> verifyReview(@Valid @PathVariable(value = "review_id") @Min(0) long reviewId) {

        Services.reviewsService.verifyReview(reviewId);

        Review verifiedReview = Services.reviewsService.getById(reviewId);

        return ResponseEntity
                .status(verifiedReview.getIsVerified() ? HttpStatus.OK : HttpStatus.NOT_MODIFIED)
                .body(String.format("Отзыв с id: %d %s верифицирован после модерации", verifiedReview.getId(),
                        verifiedReview.getIsVerified() ?"успешно" : "не был"));


    }

    // Получение всех записей с пагинацией
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ReviewRespDto> getAllReviews(@Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
                                                @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
                                                @RequestParam(value = "sort_by", defaultValue = "id")  String sortBy,
                                                @RequestParam(value = "sort_type", defaultValue = "asc") String sortType) {

        Page<Review> reviewsPage = Services.reviewsService.getAll(pageNum, limit,
                ReviewsSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(reviewsPage, () -> reviewsPage.getContent().stream().map(ReviewRespDto::new).toList());
    }
    // Получение отзывов на конкретный товар
    @GetMapping(value = "/by_product", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ReviewRespDto> getReviewsByProduct(@Valid @RequestParam(value = "product_id") @Max(1) long productId,
                                                      @RequestParam(value = "verified", required = false) Boolean verified,
                                                      @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
                                                      @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
                                                      @RequestParam(value = "sort_by", defaultValue = "id")  String sortBy,
                                                      @RequestParam(value = "sort_type", defaultValue = "asc") String sortType) {

        Page<Review> reviewsPage = Services.reviewsService.getByProductId(productId, verified, pageNum, limit,
                ReviewsSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(reviewsPage, () -> reviewsPage.getContent().stream().map(ReviewRespDto::new).toList());
    }


}
