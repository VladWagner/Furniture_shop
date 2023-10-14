package gp.wagner.backend.domain.dto.response;

import gp.wagner.backend.domain.dto.response.product_variant.ProductVariantPreviewRespDto;
import gp.wagner.backend.domain.entites.basket.Basket;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.middleware.Services;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.text.DateFormat;

//DTO для отправки на сторону клиента
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketDto {

    private long id;
    private String productName;

    private ProductVariantPreviewRespDto productVariant;


    private int productsAmount;

    private String addingDate;

    private long userId;
    private String userName;

    public BasketDto(Basket basket,ProductVariant pv, Product product) {
        this.id = basket.getId();

        //Создать dto для отправки варианта товара
        //ProductVariant pv = Services.productVariantsService.getById(basket.getProductVariant().getId());
        this.productVariant = new ProductVariantPreviewRespDto(pv);
        this.productName = product.getName();

        this.productsAmount = basket.getProductsAmount();
        this.addingDate = Utils.sdf.format(basket.getAddedDate());
        this.userId = basket.getUser().getId();
        this.userName = basket.getUser().getUserLogin();
    }
}
