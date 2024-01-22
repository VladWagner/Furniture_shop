package gp.wagner.backend.domain.dto.response.admin_panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Tuple;
import lombok.*;

// DTO для передачи кол-ва посещений за каждый день
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopProductsVariantsInBasketsRespDto {

    // id товара
    @NonNull
    @JsonProperty("product_id")
    private Long productId;

    // Название товара
    @NonNull
    @JsonProperty("product_name")
    private String productName;

    // Название варианта товара
    @NonNull
    @JsonProperty("product_variant_title")
    private String productVariantTitle;

    // id варианта товара
    @NonNull
    @JsonProperty("product_variant_id")
    private Long productVariantId;

    // Кол-во заказов
    @NonNull
    @JsonProperty("baskets_count")
    private Long basketsCount;

    public TopProductsVariantsInBasketsRespDto(Tuple tuple) {
        this.productId =           tuple.get(0, Long.class);
        this.productName =         tuple.get(1, String.class);
        this.productVariantTitle = tuple.get(2, String.class);
        this.productVariantId =    tuple.get(3, Long.class);
        this.basketsCount =        tuple.get(4, Long.class);
    }
}
