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

    // Производитель и его лого
    private ProducerInProductRespDto producer;

    //Характеристики товара
    private List<AttributeValueRespDto> attributeValues;

    //Варианты исполнения товара
    private List<ProductVariantPreviewRespDto> productVariants;

    // Изображения товара - базового варианта
    private List<ProductImageRespDto> productImage;

    public ProductDetailsRespDto(Product product, List<AttributeValueRespDto> attributeValueDtos,
                                 List<ProductVariantPreviewRespDto> productVariantsDtos) {
        this.id = product.getId();
        this.name = product.getName();
        this.categoryId = product.getCategory().getId();
        this.categoryName = product.getCategory().getName();
        this.description = product.getDescription();
        this.productImage = product.getProductVariants().get(0).getProductImages().stream().map(ProductImageRespDto::new).toList();

        this.producer = new ProducerInProductRespDto(product.getProducer().getId(), product.getProducer().getProducerName(),
                product.getProducer().getProducerLogo());
        this.isAvailable = product.getIsAvailable();
        this.attributeValues = attributeValueDtos;
        this.productVariants = productVariantsDtos;
        this.price = productVariantsDtos.get(0).getPrice();
    }

    //Фабричный метод

    public static ProductDetailsRespDto factory(Product product){

        //Получить коллекцию характеристик
        /*List<AttributeValueRespDto> attributeValues = Services.attributeValuesService.getValuesByProductId(product.getId())
                .stream().map(AttributeValueRespDto::new).toList();

        //Получить коллекцию укороченных вариантов товаров
        List<ProductVariantPreviewRespDto> productVariantPreviews = Services.productVariantsService.getByProductId(product.getId())
                .stream().map(ProductVariantPreviewRespDto::new).toList();*/

        //Получить коллекцию характеристик
        List<AttributeValueRespDto> attributeValues = product.getAttributeValues()
                .stream()
                .map(AttributeValueRespDto::new)
                .toList();

        //Получить коллекцию укороченных вариантов товаров
        List<ProductVariantPreviewRespDto> productVariantPreviews = product.getProductVariants().stream()
                .map(ProductVariantPreviewRespDto::new)
                .toList();

        return new ProductDetailsRespDto(product, attributeValues, productVariantPreviews);
    }
}

// Краткая информация о производителе
record ProducerInProductRespDto(long producerId, String producerName, String producerLogo){}
