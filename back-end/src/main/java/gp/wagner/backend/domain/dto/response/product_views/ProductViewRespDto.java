package gp.wagner.backend.domain.dto.response.product_views;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.eav.AttributeValue;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.entites.visits.ProductViews;
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
    @JsonProperty(value = "general_views_count")
    private Integer viewsCount;
    @JsonProperty(value = "avg_views_count")
    private Double avgViewsCount;

    private Long id;

    //Наименование товара
    private String name;

    //Ссылка на превью
    @JsonProperty(value = "preview_img_link")
    private String previewImgLink;

    @JsonProperty(value = "category_id")
    private long categoryId;

    @JsonProperty(value = "category_name")
    private String categoryName;

    @JsonProperty(value = "producer_name")
    private String producerName;

    //Наличие товара
    @JsonProperty(value = "is_available")
    private boolean isAvailable;

    //Флаг вывода товара
    @JsonProperty(value = "show_product")
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

    public ProductViewRespDto(ProductViews productViews) {

        Product product = productViews.getProduct();
        Category category = product.getCategory();
        Producer producer = product.getProducer();
        ProductVariant basicVariant = product.getProductVariants().get(0);

        this.id = productViews.getId();
        this.name = product.getName();
        this.categoryId = category.getId();
        this.categoryName = category.getName();
        this.producerName = producer.getProducerName();
        this.isAvailable = product.getIsAvailable();
        this.showProduct = product.getShowProduct();
        this.price = basicVariant.getPrice();
        this.previewImgLink = basicVariant.getPreviewImg();
        this.viewsCount = productViews.getCount();

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
