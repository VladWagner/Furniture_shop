package gp.wagner.backend.domain.dto.response.product_variant;


import gp.wagner.backend.domain.dto.response.product.ProductImageRespDto;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//DTO для определённого варианта товара: набор его изображений + стоимость варианта товара
//Объект нужен для замены изображений и стоимости при переключении вариантов в карточке товара (поэтому здесь нет характеристик)
public class ProductVariantDetailsRespDto {

    private Long id;

    //Наименование варианта товара
    private String title;
    //Стоимость варианта товара

    private int price;


    //Ссылка на превью
    private String previewImgLink;

    //Ссылки на изображения для галереи
    private List<ProductImageRespDto> productImages;

    private boolean showVariant;

    private boolean isDeleted;


    public ProductVariantDetailsRespDto(ProductVariant variant, List<ProductImageRespDto> images) {
        this.id = variant.getId();

        this.previewImgLink = variant.getPreviewImg();
        this.title = variant.getTitle();

        this.productImages = images;
        this.price = variant.getPrice();
    }

    public ProductVariantDetailsRespDto(ProductVariant variant) {
        this.id = variant.getId();

        this.previewImgLink = variant.getPreviewImg();
        this.title = variant.getTitle();

        this.productImages = variant.getProductImages().stream().map(ProductImageRespDto::new).toList();
        this.price = variant.getPrice();

        this.showVariant = variant.getShowVariant();
        this.isDeleted = variant.getIsDeleted() != null && variant.getIsDeleted();
    }
}
