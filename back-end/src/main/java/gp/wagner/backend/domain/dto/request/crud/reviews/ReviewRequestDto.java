package gp.wagner.backend.domain.dto.request.crud.reviews;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.validation.review_request_dto.annotations.ValidReviewRequestDto;
import gp.wagner.backend.validation.user_request_dto.annotations.ValidUserRequestDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Объект для добавления/редактирования оценки товара
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidReviewRequestDto
public class ReviewRequestDto {

    @Nullable
    private Long id;

    @NotNull
    private String text;

    @NotNull
    @JsonProperty("product_id")
    private Long productId;

    @NotNull
    @JsonProperty("user_id")
    private Long userId;

    // Номер заказа для проверки честности отзыва (при редактировании отзыва может быть null)
    @Nullable
    @JsonProperty("order_code")
    private Long orderCode;
}

