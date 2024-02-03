package gp.wagner.backend.domain.dto.response.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.eav.AttributeValue;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.infrastructure.SimpleTuple;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Comparator;
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
    @JsonProperty("discount_price")
    @Nullable
    private int discountPrice;

    // Стоимость одного или нескольких вариантов товара - то есть базовая стоимость может быть вне диапазона, но стоимость варианта может быть в нём

    @JsonProperty("mathcing_variant_price")
    @Nullable
    private int variantPrice;

    //Todo: здесь имеется немного непонятная запись - получение экземпляров класса AttributeValue по заданным через hardcode индексам
    //В чём и вопрос, с чего вдруг значения ширины, высоты и глубины будут находится именно по заданным индексам
    //P.S. хотя возможно, что CU (create, update) построен так, что при любом изменении и добавлении товара,
    //будут сначала добавляться его габариты
    public ProductPreviewRespDto(Product product, int price, String previewImage, List<AttributeValue> attributeValues) {

        Category category = product.getCategory();
        Producer producer = product.getProducer();

        this.id = product.getId();
        this.name = product.getName();
        this.categoryId = category.getId();
        this.categoryName = category.getName();
        this.producerId = producer.getId();
        this.producerName = producer.getProducerName()/*"Pivedenne"*/;
        this.isAvailable = product.getIsAvailable();
        this.showProduct = product.getShowProduct();
        this.price = price;
        this.previewImgLink = previewImage;
         /*this.sizes = String.format("Размер см: Ш %d x В %d x Г %d", attributeValues.stream().filter(av -> av.getAttribute().getAttributeName().equalsIgnoreCase("ширина")).findFirst().get().getIntValue(),
                                                                    attributeValues.stream().filter(av -> av.getAttribute().getAttributeName().equalsIgnoreCase("Высота")).findFirst().get().getIntValue(),
                                                                    attributeValues.stream().filter(av -> av.getAttribute().getAttributeName().equalsIgnoreCase("глубина")).findFirst().get().getIntValue());*/
        this.sizes = attributeValues.size() > 0? String.format("Размер см: Ш %d x В %d x Г %d", attributeValues.get(0).getIntValue(),
                                                                    attributeValues.get(1).getIntValue(),
                                                                    attributeValues.get(2).getIntValue()) : "Размер неизвестен";


    }
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
         this.sizes = avsList.size() > 0? String.format("Размер см: Ш %d x В %d x Г %d", avsList.get(0).getIntValue(),
                                                                                         avsList.get(1).getIntValue(),
                                                                                         avsList.get(2).getIntValue()) : "Размер неизвестен";

    }
    public ProductPreviewRespDto(Product product, SimpleTuple<Integer, Integer> pricesRange) {

        this(product);

        // Найти цену варианта в диапазоне, цена базового варианта не входит в искомый диапазон
        if (this.price <= pricesRange.getValue1() || this.price >= pricesRange.getValue2()) {
            ProductVariant productVariant = product.getProductVariants()
                                                   .stream()
                                                   .filter(pv -> pv.getPrice() >= pricesRange.getValue1() && pv.getPrice() <= pricesRange.getValue2())
                                                   .findFirst().orElse(null);

            variantPrice = productVariant != null ? productVariant.getPrice() : 0;
        }

    }
}
