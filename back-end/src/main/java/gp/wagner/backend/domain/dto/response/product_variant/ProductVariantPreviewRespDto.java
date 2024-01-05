package gp.wagner.backend.domain.dto.response.product_variant;


import gp.wagner.backend.domain.entites.products.ProductVariant;
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

    //Выводить ли данный вариант
    private boolean showVariant;

    public ProductVariantPreviewRespDto(ProductVariant variant) {
        this.id = variant.getId();
        this.productId = variant.getProduct().getId();
        this.productName = variant.getProduct().getName();
        this.title = variant.getTitle();
        this.previewImgLink = variant.getPreviewImg();
        this.price = variant.getPrice();
        this.showVariant = variant.getShowVariant();
    }
}
