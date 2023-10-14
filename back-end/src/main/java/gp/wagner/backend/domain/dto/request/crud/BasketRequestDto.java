package gp.wagner.backend.domain.dto.request.crud;

import gp.wagner.backend.infrastructure.Utils;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.text.ParseException;
import java.util.Date;

//Объект для добавления/редактирования товара в корзину
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BasketRequestDto {

    //Для редактирования корзины
    @Nullable
    private Long id;

    @NotNull
    @Min(1)
    private Long productVariantId;
    @NotNull
    @Min(1)
    private Long userId;

    @Min(0)
    private int productsAmount;

    //Дата задаётся строкой - на стороне бэка будет производиться создание нужного объекта
    @NotBlank
    private String addedDate;

    //Получение даты из строки
    public Date getAddedDate(){

        try {
            return Utils.sdf.parse(addedDate);
        } catch (ParseException e) {
            return null;
        }
    }

}
