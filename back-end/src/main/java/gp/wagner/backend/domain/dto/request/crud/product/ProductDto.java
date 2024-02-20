package gp.wagner.backend.domain.dto.request.crud.product;

import gp.wagner.backend.domain.dto.request.crud.AttributeValueDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

//Объект для добавления/редактирования товара
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    //Для редактирования товара
    @Nullable
    private Long id;

    @NotBlank
    private String name;
    @NotNull
    private String description;

    @NotNull
    private String variantTitle;

    //Для редактирования базового варианта при редактировании характеристик
    @Nullable
    private Long variantId = null;

    // Данный атрибуты будут заваться под определённую категорию на фронте
    @NotNull
    private List<AttributeValueDto> attributes;

    //Удаленные характеристики - возможность удаления атрибута при редактировании (массив идентификаторов)
    @Nullable
    private Long[] deletedAttributesValues;

    @NotNull
    @Min(1)
    private Long categoryId;

    @Nullable
    private String categoryName;

    @NotNull
    @Min(1)
    private Long producerId;

    @Nullable
    private String producerName;

    //Имеется ли в наличии
    @NotNull
    private Boolean isAvailable;

    @NotNull
    private Boolean showProduct;

    @NotNull
    private Integer price;

}
