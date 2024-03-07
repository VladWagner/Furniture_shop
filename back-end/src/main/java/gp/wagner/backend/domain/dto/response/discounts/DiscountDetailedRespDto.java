package gp.wagner.backend.domain.dto.response.discounts;

import gp.wagner.backend.domain.dto.response.products.SimpleProductRespDto;
import gp.wagner.backend.domain.entites.products.Discount;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import jakarta.persistence.Column;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

// DTO для возврата детальной информации о скидке
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscountDetailedRespDto extends DiscountRespDto {

    // Товары и их варианты для которых задана скидка
    @Column(name = "products_list")
    private List<SimpleProductRespDto> productsList;


    public DiscountDetailedRespDto(Discount discount) {
        super(discount);
        this.productsList = discount.getProductVariants()
                .stream()
                .collect(Collectors.groupingBy(ProductVariant::getProduct))
                //.collect(Collectors.groupingBy(pv -> pv.getProduct().getId()))
                .keySet()
                .stream()
                .map(SimpleProductRespDto::new).toList();
    }
}