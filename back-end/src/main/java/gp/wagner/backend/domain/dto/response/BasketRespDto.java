package gp.wagner.backend.domain.dto.response;

import gp.wagner.backend.domain.dto.response.product_variants.ProductVariantPreviewRespDto;
import gp.wagner.backend.domain.entites.baskets.Basket;
import gp.wagner.backend.domain.entites.baskets.BasketAndProductVariant;
import gp.wagner.backend.infrastructure.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

//DTO для отправки на сторону клиента
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketRespDto {

    private long id;

    // Внутренний клас для хранения DTO варианта товара для ответа и его количества в понятном формате
    private static class ProductVariantResp{
        public ProductVariantPreviewRespDto productVariantDto;

        public int count;

        public ProductVariantResp(BasketAndProductVariant bpv) {
            this.productVariantDto = new ProductVariantPreviewRespDto(bpv.getProductVariant());
            this.count = bpv.getProductsAmount();
        }
        public ProductVariantResp(ProductVariantPreviewRespDto productVariantDto, int count) {
            this.productVariantDto = productVariantDto;
            this.count = count;
        }
    }

    private List<ProductVariantResp> productVariants;

    // Сумма цен товаров в корзине
    private int sum;

    private String addingDate;

    private long userId;
    private String userName;

    public BasketRespDto(Basket basket) {
        this.id = basket.getId();

        // Здесь никак не фильтруются товары по флагу удаления - для возможности демонстрации того, что товар удалён на фронте
        // Т.е. даже удалённый товар хранится, но по идее его
        this.productVariants = basket.getBasketAndPVList()
                .stream()
                .map(ProductVariantResp::new)
                .toList();

        this.sum = basket.getSum();
        this.addingDate = Utils.sdf.format(basket.getAddedDate());
        this.userId = basket.getUser().getId();
        this.userName = basket.getUser().getUserLogin();
    }
}
