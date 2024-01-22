package gp.wagner.backend.domain.dto.response.admin_panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.infrastructure.SimpleTuple;
import jakarta.persistence.Tuple;
import lombok.*;

import java.util.Date;

// DTO для передачи кол-ва посещений за каждый день
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductsOrdersCountRespDto {

    // id товара
    @NonNull
    @JsonProperty("product_id")
    private Long productId;

    // Название товара
    @NonNull
    @JsonProperty("product_name")
    private String productName;

    // Кол-во заказов
    @NonNull
    @JsonProperty("orders_count")
    private Long ordersCount;

    public ProductsOrdersCountRespDto(Tuple tuple) {
        this.productId = tuple.get(0, Long.class);
        this.productName = tuple.get(1, String.class);
        this.ordersCount = tuple.get(2, Long.class);
    }
}
