package gp.wagner.backend.domain.dto.response.ratings;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.ratings.Rating;
import gp.wagner.backend.validation.user_request_dto.annotations.ValidUserRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidUserRequestDto
public class RatingRespDto {

    private Long id;

    private Integer rating;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("updated_at")
    private Date updatedAt;

    public RatingRespDto(Rating rating) {
        Product product = rating.getProduct();

        this.id = rating.getId();
        this.rating = rating.getRating();
        this.productId = product.getId();
        this.productName = product.getName();
        this.userId = rating.getUser().getId();
        this.createdAt = rating.getCreatedAt();
        this.updatedAt = rating.getUpdatedAt();
    }
}

