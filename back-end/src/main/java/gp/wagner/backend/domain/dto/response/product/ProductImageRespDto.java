package gp.wagner.backend.domain.dto.response.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import gp.wagner.backend.domain.entites.products.ProductImage;
import jakarta.annotation.Nullable;
import lombok.*;

import java.util.Date;

//DTO-объект для передачи ссылки на изображение + порядок вывода изображения в галереи
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductImageRespDto {

    @Nullable
    private Long id;
    private String imageLink;

    //Порядок вывода изображения в карточке товара
    private int order;

    public ProductImageRespDto(ProductImage productImage) {
        this.id = productImage.getId();
        this.imageLink = productImage.getImgLink();
        this.order = productImage.getImgOrder();
    }
}
