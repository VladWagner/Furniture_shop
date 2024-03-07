package gp.wagner.backend.domain.dto.response.product_variants;


import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//DTO для передачи данных предосмотра вариантов товара (в карточке товара)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantPreviewRespDto {

    private Long id;

    private Long productId;

    //Наименование товара
    private String productName;

    //Наименование варианта товара
    private String title;

    //Ссылка на превью - первое изображение варианта
    private String previewImgLink;

    //Стоимость товара
    private int price;

    // Стоимость при наличии скидки
    @JsonProperty("discount_price")
    @Nullable
    private int discountPrice;

    // Скидка на вариант
    @Nullable
    @JsonProperty("discount_percent")
    private Float discountPercent;

    //Выводить ли данный вариант. В заказах и товарах можно скрывать за полупрозрачным фоном и писать, что товар закончился и сумма пересчитана
    private boolean showVariant;

    private boolean isDeleted;

    public ProductVariantPreviewRespDto(ProductVariant variant) {
        this.id = variant.getId();
        this.productId = variant.getProduct().getId();
        this.productName = variant.getProduct().getName();
        this.title = variant.getTitle();
        this.previewImgLink = variant.getPreviewImg();
        this.price = variant.getPrice();
        this.showVariant = variant.getShowVariant();
        this.isDeleted = variant.getIsDeleted() != null && variant.getIsDeleted();

        // Если задана скидка и при этом срок её действия не истёк
        if (variant.getDiscount() != null && !variant.getDiscount().isExpired()){
            this.discountPrice = variant.getPriceWithDiscount();
            this.discountPercent = variant.getDiscount().getPercentage();
        }
    }
}
