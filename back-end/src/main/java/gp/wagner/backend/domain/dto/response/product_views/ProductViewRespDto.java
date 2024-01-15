package gp.wagner.backend.domain.dto.response.product_views;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.eav.AttributeValue;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
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
public class ProductViewRespDto {


    // Количество просмотров данного товара
    private Integer viewsCount;
    private Double avgViewsCount;

    private Long id;

    //Наименование товара
    private String name;

    //Ссылка на превью
    private String previewImgLink;

    private long categoryId;
    private String categoryName;

    private String producerName;

    //Наличие товара
    private boolean isAvailable;

    //Флаг вывода товара
    private boolean showProduct;

    //Стоимость товара
    private int price;


    public ProductViewRespDto(Product product) {

        Category category = product.getCategory();
        Producer producer = product.getProducer();
        ProductVariant basicVariant = product.getProductVariants().get(0);

        this.id = product.getId();
        this.name = product.getName();
        this.categoryId = category.getId();
        this.categoryName = category.getName();
        this.producerName = producer.getProducerName();
        this.isAvailable = product.getIsAvailable();
        this.showProduct = product.getShowProduct();
        this.price = basicVariant.getPrice();
        this.previewImgLink = basicVariant.getPreviewImg();

    }
    public ProductViewRespDto(Product product, int viewsCount) {

        this(product);
        this.viewsCount = viewsCount;

    }
    public ProductViewRespDto(Product product, double avgViewsCount) {

        this(product);
        this.avgViewsCount = avgViewsCount;

    }


}
