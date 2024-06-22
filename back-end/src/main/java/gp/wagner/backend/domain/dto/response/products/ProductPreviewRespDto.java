package gp.wagner.backend.domain.dto.response.products;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.dto.response.ratings.RatingStatisticsRespDto;
import gp.wagner.backend.domain.entities.categories.Category;
import gp.wagner.backend.domain.entities.eav.AttributeValue;
import gp.wagner.backend.domain.entities.products.Producer;
import gp.wagner.backend.domain.entities.products.Product;
import gp.wagner.backend.domain.entities.products.ProductVariant;
import gp.wagner.backend.infrastructure.SimpleTuple;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

//Объект передачи и вывода товара в списке товаров в виде карточки
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductPreviewRespDto {

    private Long id;

    //Наименование товара
    private String name;

    //Ссылка на превью
    @JsonProperty("preview_img_link")
    private String previewImgLink;

    //Строковое значение размеров
    private String sizes;

    //Связующие свойство категории товара.
    @JsonProperty("category_id")
    private long categoryId;

    @JsonProperty("category_name")
    private String categoryName;

    //Связующие свойство производитель товара
    @JsonProperty("producer_id")
    private long producerId;
    @JsonProperty("producer_name")
    private String producerName;

    //Наличие товара
    @JsonProperty("is_available")
    private boolean isAvailable;

    //Флаг вывода товара
    @JsonProperty("show_product")
    private boolean showProduct;

    //Стоимость товара
    private int price;

    //Стоимость товара по скидке
    @Nullable
    @JsonProperty("discount_price")
    private Integer discountPrice;

    // Скидка на товар/вариант
    @Nullable
    @JsonProperty("discount_percent")
    private Float discountPercent;

    // Стоимость одного или нескольких вариантов товара - то есть базовая стоимость может быть вне диапазона, но стоимость варианта может быть в нём
    @Nullable
    @JsonProperty("matching_variant_price")
    private Integer variantPrice;

    // Статистика по оценкам товара

    @JsonProperty("rating_statistics")
    private RatingStatisticsRespDto ratingStatisticsDto;

    //Todo: здесь имеется немного непонятная запись - получение экземпляров класса AttributeValue по заданным через hardcode индексам
    //В чём и вопрос, с чего вдруг значения ширины, высоты и глубины будут находится именно по заданным индексам
    //P.S. хотя возможно, что CU (create, update) построен так, что при любом изменении и добавлении товара,
    //будут сначала добавляться его габариты
    public ProductPreviewRespDto(Product product) {

        Category category = product.getCategory();
        Producer producer = product.getProducer();
        ProductVariant basicVariant = product.getProductVariants().get(0);
        List<AttributeValue> avsList = product.getAttributeValues();

        this.id = product.getId();
        this.name = product.getName();
        this.categoryId = category.getId();
        this.categoryName = category.getName();
        this.producerId = producer.getId();
        this.producerName = producer.getProducerName();
        this.isAvailable = product.getIsAvailable();
        this.showProduct = product.getShowProduct();
        this.price = basicVariant.getPrice();
        this.previewImgLink = basicVariant.getPreviewImg();
        this.ratingStatisticsDto = product.getRatingStatistics() != null ?
                new RatingStatisticsRespDto(product.getRatingStatistics()) : null;
        /*this.sizes = String.format("Размер см: Ш %d x В %d x Г %d", attributeValues.stream().filter(av -> av.getAttribute().getAttributeName().equalsIgnoreCase("ширина")).findFirst().get().getIntValue(),
                                                                    attributeValues.stream().filter(av -> av.getAttribute().getAttributeName().equalsIgnoreCase("Высота")).findFirst().get().getIntValue(),
                                                                    attributeValues.stream().filter(av -> av.getAttribute().getAttributeName().equalsIgnoreCase("глубина")).findFirst().get().getIntValue());*/
        this.sizes = avsList.size() > 0? String.format("Размер см: Ш %d x В %d x Г %d", avsList.get(0).getIntValue(),
                                                                                         avsList.get(1).getIntValue(),
                                                                                         avsList.get(2).getIntValue()) : "Размер неизвестен";

        // Если скидка задана хоть в одном из вариантов
        if (basicVariant.getDiscount() == null){
            ProductVariant variantWithDiscount = product.getProductVariants()
                    .stream()
                    .filter(pv -> pv.getDiscount() != null).findFirst().orElse(null);

            // Цену оставим null, чтобы на фронте было понятно, что скидка задана не для базового варианта
            this.discountPercent = variantWithDiscount != null && !variantWithDiscount.getDiscount().isExpired() ?
                    variantWithDiscount.getDiscount().getPercentage() : null;

        }else if (!basicVariant.getDiscount().isExpired()) {
            this.discountPercent = basicVariant.getDiscount().getPercentage();
            this.discountPrice = basicVariant.getPriceWithDiscount();
        }

    }
    public ProductPreviewRespDto(Product product, SimpleTuple<Integer, Integer> pricesRange) {

        this(product);

        // Найти цену варианта в диапазоне, цена базового варианта или его цена со скидкой не входит в искомый диапазон
        boolean discountPriceNotInRange = this.discountPrice == null || (this.discountPrice <= pricesRange.getValue1() || this.discountPrice >= pricesRange.getValue2());
        if ((this.price <= pricesRange.getValue1() || this.price >= pricesRange.getValue2()) && discountPriceNotInRange) {
            ProductVariant productVariant = product.getProductVariants()
                                                   .stream()
                                                   .filter(pv -> pv.getPriceWithDiscount() >= pricesRange.getValue1() && pv.getPrice() <= pricesRange.getValue2())
                                                   .findFirst().orElse(null);

            variantPrice = productVariant != null ? productVariant.getPriceWithDiscount() : 0;
        }

    }
}
