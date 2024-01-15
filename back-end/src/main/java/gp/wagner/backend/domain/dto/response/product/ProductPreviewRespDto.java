package gp.wagner.backend.domain.dto.response.product;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.eav.AttributeValue;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class ProductPreviewRespDto {

    private Long id;

    //Наименование товара
    private String name;

    //Ссылка на превью
    private String previewImgLink;

    //Строковое значение размеров
    private String sizes;

    //Связующие свойство категории товара
    private long categoryId;
    private String categoryName;

    //Связующие свойство производитель товара
    private long producerId;
    private String producerName;

    //Наличие товара
    private boolean isAvailable;

    //Флаг вывода товара
    private boolean showProduct;

    //Стоимость товара
    private int price;

    //Стоимость товара по скидке
    @Nullable
    private int discountPrice;

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
}
