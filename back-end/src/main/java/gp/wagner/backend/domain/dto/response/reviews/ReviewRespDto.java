package gp.wagner.backend.domain.dto.response.reviews;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entities.reviews.Review;
import gp.wagner.backend.validation.review_request_dto.annotations.ValidReviewRequestDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

// DTO для отправки отзыва на конкретный товар
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidReviewRequestDto
public class ReviewRespDto {

    @Nullable
    private Long id;

    @NotNull
    private String text;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("user_id")
    private Long userId;

    @Nullable
    @JsonProperty("review_images")
    private List<ReviewImageRespDto> reviewImages;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("updated_at")
    private Date updatedAt;

    public ReviewRespDto(Review review) {
        this.id = review.getId();
        this.text = review.getText();
        this.productId = review.getProduct().getId();
        this.userId = review.getUser().getId();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
        this.reviewImages = review.getReviewImages() != null ? review.getReviewImages().stream().map(ReviewImageRespDto::new).toList() : null;
    }
}

