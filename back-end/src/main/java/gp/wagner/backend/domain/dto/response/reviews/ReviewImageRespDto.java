package gp.wagner.backend.domain.dto.response.reviews;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entities.reviews.ReviewImage;
import jakarta.annotation.Nullable;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImageRespDto {

    @Nullable
    private Long id;

    @NonNull
    @JsonProperty("image_link")
    private String imageLink;

    // Порядковый номер
    @JsonProperty("img_order")
    private int imgOrder;

    public ReviewImageRespDto(ReviewImage image) {
        this.id = image.getId();
        this.imageLink = image.getImgLink();
        this.imgOrder = image.getImgOrder();
    }
}
