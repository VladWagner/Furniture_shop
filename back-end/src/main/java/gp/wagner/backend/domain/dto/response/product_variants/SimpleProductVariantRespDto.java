package gp.wagner.backend.domain.dto.response.product_variants;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Простой DTO для вывода варианта в таблицах админ-панели
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleProductVariantRespDto {

    @JsonProperty("variant_id")
    private Long id;

    @JsonProperty("product_id")
    private long productId;

    @JsonProperty("category_id")
    private long categoryId;

    // Наименование товара
    @JsonProperty("product_name")
    private String productName;

    // Наименование категории товара
    @JsonProperty("category_name")
    private String categoryName;

    //Наименование варианта товара
    private String title;

    private int price;

    @Nullable
    @JsonProperty("price_with_discount")
    private Integer priceWithDiscount;

    // Скидка на вариант
    @Nullable
    @JsonProperty("discount_percent")
    private Float discountPercent;

    //Выводить ли данный вариант. В заказах и товарах можно скрывать за полупрозрачным фоном и писать, что товар закончился и сумма пересчитана

    @JsonProperty("show_variant")
    private boolean showVariant;


    @JsonProperty("is_deleted")
    private boolean isDeleted;


    public SimpleProductVariantRespDto(ProductVariant variant) {
        Product product = variant.getProduct();
        Category category = product.getCategory();

        this.id = variant.getId();
        this.productId = product.getId();
        this.categoryId = category.getId();
        this.productName = product.getName();
        this.categoryName = category.getName();
        this.title = variant.getTitle();
        this.price = variant.getPrice();

        if (variant.getDiscount() != null && !variant.getDiscount().isExpired()) {
            this.priceWithDiscount = variant.getPriceWithDiscount();
            this.discountPercent = variant.getDiscount().getPercentage();
        }

        this.showVariant = variant.getShowVariant();
        this.isDeleted = variant.getIsDeleted() != null && variant.getIsDeleted();
    }
}
