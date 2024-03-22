package gp.wagner.backend.domain.dto.request.crud;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

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

    // Ассоциативная коллекция - id варианта товара + его количество
    @NotNull
    private Map<Integer, Integer> productVariantIdAndCount;

    @Nullable
    private Long userId;

}
