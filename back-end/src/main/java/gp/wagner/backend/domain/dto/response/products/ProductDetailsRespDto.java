package gp.wagner.backend.domain.dto.response.products;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.dto.response.discounts.DiscountRespDto;
import gp.wagner.backend.domain.dto.response.product_attributes.AttributeValueRespDto;
import gp.wagner.backend.domain.dto.response.product_variants.ProductVariantPreviewRespDto;
import gp.wagner.backend.domain.dto.response.ratings.RatingStatisticsRespDto;
import gp.wagner.backend.domain.entites.products.Product;
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
public class ProductDetailsRespDto {

    private long id;

    private String name;
    private int price;

    @JsonProperty("discount_price")
    @Nullable
    private int discountPrice;

    //Категории товара
    @JsonProperty("category_id")
    private long categoryId;

    @JsonProperty("category_name")
    private String categoryName;

    //Описание
    private String description;

    @JsonProperty("is_available")
    private boolean isAvailable;

    // Скидка на товар
    @Nullable
    @JsonProperty("discount")
    private DiscountRespDto discountRespDto;

    // Производитель и его лого
    private ProducerInProductRespDto producer;

    //Характеристики товара
    @JsonProperty("attribute_values")
    private List<AttributeValueRespDto> attributeValues;

    //Варианты исполнения товара
    @JsonProperty("product_variants")
    private List<ProductVariantPreviewRespDto> productVariants;

    // Изображения товара - базового варианта
    @JsonProperty("product_images")
    private List<ProductImageRespDto> productImages;

    // Статистика по оценкам товара
    @JsonProperty(value = "rating_statistics", index = 3)
    private RatingStatisticsRespDto ratingStatisticsDto;

    public ProductDetailsRespDto(Product product, List<AttributeValueRespDto> attributeValueDtos,
                                 List<ProductVariantPreviewRespDto> productVariantsDtos) {
        this.id = product.getId();
        this.name = product.getName();
        this.categoryId = product.getCategory().getId();
        this.categoryName = product.getCategory().getName();
        this.description = product.getDescription();
        this.productImages = product.getProductVariants().get(0).getProductImages().stream().map(ProductImageRespDto::new).toList();

        this.producer = new ProducerInProductRespDto(product.getProducer().getId(), product.getProducer().getProducerName(),
                product.getProducer().getProducerLogo());
        this.isAvailable = product.getIsAvailable();
        this.attributeValues = attributeValueDtos;

        this.productVariants = productVariantsDtos;
        this.ratingStatisticsDto = product.getRatingStatistics() != null ?
                new RatingStatisticsRespDto(product.getRatingStatistics()) : null;

        ProductVariant productVariant = product.getProductVariants().get(0);
        this.price = productVariant.getPrice();

        // Если у базового варианта задана скидка и при этом она не просрочена
        if (productVariant.getDiscount() != null && !productVariant.getDiscount().isExpired()){
            this.discountPrice = productVariant.getPriceWithDiscount();
            this.discountRespDto = new DiscountRespDto(productVariant.getDiscount());
        }

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
