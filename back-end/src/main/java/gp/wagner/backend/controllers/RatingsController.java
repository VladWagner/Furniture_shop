package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.ratings.RatingRequestDto;
import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.dto.response.ratings.RatingRespDto;
import gp.wagner.backend.domain.entites.ratings.Rating;
import gp.wagner.backend.infrastructure.enums.sorting.DiscountsSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.RatingsSortEnum;
import gp.wagner.backend.middleware.Services;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/products_ratings")
public class RatingsController {

    // Задать/изменить оценку товара
    @PostMapping(value = "/set_rating")
    public ResponseEntity<RatingRespDto> addOrUpdateRating(@RequestBody RatingRequestDto ratingRequestDto) throws Exception{

        Rating rating = Services.ratingsService.createOrUpdate(ratingRequestDto);

        return ResponseEntity.ok(new RatingRespDto(rating));
    }

    // Убрать оценку
    @PutMapping(value = "/remove_rating")
    public ResponseEntity<String> removeRating(@Valid @RequestParam(value = "product_id") @Min(0) long productId,
                                               @Valid @RequestParam(value = "user_id") @Min(0) long userId) {

        Services.ratingsService.removeRating(productId, userId);

        Rating removedRating = Services.ratingsService.getByProductAndUserId(productId, userId);

        boolean isRemoved = removedRating == null;
        return ResponseEntity
                .status(isRemoved ? HttpStatus.OK : HttpStatus.NOT_MODIFIED)
                .body(String.format("Оценка заданная пользователем %d на товар с id: %d %s убрана", userId, productId,
                        !isRemoved ? "не была" : "успешно"));


    }

    // Получение всех записей с пагинацией
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<RatingRespDto> getAllRatings(@Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
                                                @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
                                                @RequestParam(value = "sort_by", defaultValue = "id")  String sortBy,
                                                @RequestParam(value = "sort_type", defaultValue = "asc") String sortType) {

        Page<Rating> ratingsPage = Services.ratingsService.getAll(pageNum, limit,
                RatingsSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(ratingsPage, () -> ratingsPage.getContent().stream().map(RatingRespDto::new).toList());
    }


}
