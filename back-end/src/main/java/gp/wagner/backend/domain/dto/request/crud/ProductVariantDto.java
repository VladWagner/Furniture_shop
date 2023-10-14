package gp.wagner.backend.domain.dto.request.crud;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDto {

    //id варианта - для редактирования
    @Nullable
    private long id;

    //id товара, для которого создаётся/изменяется варианта
    @Min(1)
    private long productId;

    //id категории товара
    @Min(1)
    private long categoryId;

    //Название варианта товара
    @NotNull
    private String title;

    //Стоимость варианта товара
    @Min(1)
    private int price;

    //Показывать ли товар - в большинстве случаев для редактирования
    @Nullable
    private Boolean showProductVariant;
}
