package gp.wagner.backend.domain.dto.response.product_variants;


import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.dto.response.discounts.DiscountRespDto;
import gp.wagner.backend.domain.dto.response.products.ProductImageRespDto;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
// DTO для определённого варианта товара: набор его изображений + стоимость варианта товара
// Объект нужен для замены изображений и стоимости при переключении вариантов в карточке товара (поэтому здесь нет характеристик)
public class ProductVariantDetailsRespDto {

    private Long id;

    @JsonProperty("product_id")
    private Long productId;

    //Наименование варианта товара
    private String title;
    //Стоимость варианта товара

    private int price;

    // Поле цены при наличии скидки
    @JsonProperty("discount_price")
    @Nullable
    private int discountPrice;

    // Скидка на вариант
    @Nullable
    @JsonProperty("discount")
    private DiscountRespDto discountRespDto;

    //Ссылка на превью
    @JsonProperty("preview_img_link")
    private String previewImgLink;

    //Ссылки на изображения для галереи
    private List<ProductImageRespDto> productImages;

    @JsonProperty("show_variant")
    private boolean showVariant;

    @JsonProperty("is_deleted")
    private boolean isDeleted;


    public ProductVariantDetailsRespDto(ProductVariant variant, List<ProductImageRespDto> images) {
        this.id = variant.getId();

        this.previewImgLink = variant.getPreviewImg();
        this.title = variant.getTitle();

        this.productImages = images;
        this.price = variant.getPrice();

        // Если задана скидка и при этом срок действия скидки не истёк
        if (variant.getDiscount() != null && !variant.getDiscount().isExpired()){
            this.discountPrice = variant.getPriceWithDiscount();
            this.discountRespDto = new DiscountRespDto(variant.getDiscount());
        }
    }

    public ProductVariantDetailsRespDto(ProductVariant variant) {
        this.id = variant.getId();
        this.productId = variant.getProduct().getId();

        this.previewImgLink = variant.getPreviewImg();
        this.title = variant.getTitle();

        this.productImages = variant.getProductImages().stream().map(ProductImageRespDto::new).toList();
        this.price = variant.getPrice();

        this.showVariant = variant.getShowVariant();
        this.isDeleted = variant.getIsDeleted() != null && variant.getIsDeleted();

        // Если задана скидка
        if (variant.getDiscount() != null){
            this.discountPrice = variant.getPriceWithDiscount();
            this.discountRespDto = new DiscountRespDto(variant.getDiscount());
        }
    }
}
