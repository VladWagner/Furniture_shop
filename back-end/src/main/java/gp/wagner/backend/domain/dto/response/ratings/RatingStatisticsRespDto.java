package gp.wagner.backend.domain.dto.response.ratings;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entities.ratings.RatingStatistics;
import gp.wagner.backend.validation.user_request_dto.annotations.ValidUserRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidUserRequestDto
public class RatingStatisticsRespDto {

    private Long id;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("avg_rating")
    private Float avgRating;

    @JsonProperty("ratings_amount")
    private Integer ratingsAmount;

    public RatingStatisticsRespDto(RatingStatistics ratingStatistics) {

        this.id = ratingStatistics.getId();
        this.avgRating = ratingStatistics.getAvg() > 0 ? ratingStatistics.getAvg() : 0;
        this.ratingsAmount = ratingStatistics.getAmount() > 0 ? ratingStatistics.getAmount() : 0;
        this.productId = ratingStatistics.getProduct().getId()  ;
    }
}

