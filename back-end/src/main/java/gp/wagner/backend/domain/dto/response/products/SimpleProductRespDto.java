package gp.wagner.backend.domain.dto.response.products;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.dto.response.product_variants.SimpleProductVariantRespDto;
import gp.wagner.backend.domain.dto.response.ratings.RatingStatisticsRespDto;
import gp.wagner.backend.domain.entities.categories.Category;
import gp.wagner.backend.domain.entities.products.Discount;
import gp.wagner.backend.domain.entities.products.Producer;
import gp.wagner.backend.domain.entities.products.Product;
import gp.wagner.backend.domain.entities.products.ProductVariant;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// Простой DTO для вывода товара в справочных таблицах и списках admin-панели
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleProductRespDto {

    private Long id;

    //Наименование товара
    private String name;


    //Связующие свойство категории товара.
    @JsonProperty("category_id")
    private long categoryId;

    @JsonProperty("category_name")
    private String categoryName;

    // Связующие свойство производитель товара
    @JsonProperty("producer_id")
    private long producerId;
    @JsonProperty("producer_name")
    private String producerName;

    // Наличие товара
    @JsonProperty("is_available")
    private boolean isAvailable;

    // Флаг вывода товара
    @JsonProperty("show_product")
    private boolean showProduct;

    // Стоимость товара
    private int price;

    //Стоимость товара по скидке
    @JsonProperty("discount_price")
    @Nullable
    private int discountPrice;

    // Скидка на товар/вариант
    @Nullable
    @JsonProperty("discount_percent")
    private Float discountPercent;

    // Статистика по оценкам товара
    @JsonProperty("rating_statistics")
    private RatingStatisticsRespDto ratingStatisticsDto;

    // Варианты данного товара
    private List<SimpleProductVariantRespDto> variants;

    public SimpleProductRespDto(Product product) {

        Category category = product.getCategory();
        Producer producer = product.getProducer();
        ProductVariant basicVariant = product.getProductVariants().get(0);

        this.id = product.getId();
        this.name = product.getName();
        this.categoryId = category.getId();
        this.categoryName = category.getName();
        this.producerId = producer.getId();
        this.producerName = producer.getProducerName();
        this.isAvailable = product.getIsAvailable();
        this.showProduct = product.getShowProduct();
        this.price = basicVariant.getPrice();
        this.ratingStatisticsDto = product.getRatingStatistics() != null ?
                new RatingStatisticsRespDto(product.getRatingStatistics()) : null;

        variants = product.getProductVariants()
                .stream()
                .map(SimpleProductVariantRespDto::new)
                .toList();

        // Если скидка задана хоть в одном из вариантов
        if (basicVariant.getDiscount() == null){
            ProductVariant variantWithDiscount = product.getProductVariants()
                    .stream()
                    .filter(pv -> pv.getDiscount() != null).findFirst().orElse(null);

            Discount discount = variantWithDiscount != null && !variantWithDiscount.getDiscount().isExpired() ?
                    variantWithDiscount.getDiscount() : null;

            // Задать только % без стоимости товара со скидкой, поскольку для базовго варианта скидки нет
            this.discountPercent = discount != null ? discount.getPercentage() : null;

        }else if (!basicVariant.getDiscount().isExpired()) {
            this.discountPercent = basicVariant.getDiscount().getPercentage();
            this.discountPrice = basicVariant.getPriceWithDiscount();
        }

    }
}