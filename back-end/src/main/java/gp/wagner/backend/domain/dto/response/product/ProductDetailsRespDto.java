package gp.wagner.backend.domain.dto.response.product;

import gp.wagner.backend.domain.dto.response.AttributeValueRespDto;
import gp.wagner.backend.domain.dto.response.product_variant.ProductVariantPreviewRespDto;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.middleware.Services;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailsRespDto {

    private long id;

    private String name;
    private int price;

    //Категории товара
    private long categoryId;
    private String categoryName;

    //Описание
    private String description;

    private boolean isAvailable;

    //Характеристики товара
    private List<AttributeValueRespDto> attributeValues;

    //Варианты исполнения товара
    private List<ProductVariantPreviewRespDto> productVariants;

    public ProductDetailsRespDto(Product product, List<AttributeValueRespDto> attributeValueDtos,
                                 List<ProductVariantPreviewRespDto> productVariantsDtos) {
        this.id = product.getId();
        this.name = product.getName();
        this.categoryId = product.getCategory().getId();
        this.categoryName = product.getCategory().getName();
        this.description = product.getDescription();
        this.isAvailable = product.getIsAvailable();
        this.attributeValues = attributeValueDtos;
        this.productVariants = productVariantsDtos;
        this.price = productVariantsDtos.get(0).getPrice();
    }

    //Фабричный метод
    public static ProductDetailsRespDto factory(Product product){

        //Получить коллекцию характеристик
        List<AttributeValueRespDto> attributeValues = Services.attributeValuesService.getValuesByProductId(product.getId())
                .stream().map(AttributeValueRespDto::new).toList();

        //Получить коллекцию укороченных вариантов товаров
        List<ProductVariantPreviewRespDto> productVariantPreviews = Services.productVariantsService.getByProductId(product.getId())
                .stream().map(ProductVariantPreviewRespDto::new).toList();

        return new ProductDetailsRespDto(product, attributeValues, productVariantPreviews);
    }
}
