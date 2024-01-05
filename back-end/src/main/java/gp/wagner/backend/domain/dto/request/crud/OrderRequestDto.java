package gp.wagner.backend.domain.dto.request.crud;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

//Объект для добавления/редактирования заказа
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {

    //Для редактирования заказа
    @Nullable
    private Long id;

    @NotNull
    private Map<Integer, Integer> productVariantIdAndCount;

    // Объект покупателя (может создаваться либо уже существовать) - агрегация
    @NotNull
    private CustomerDto customer;

    @Nullable
    private Long code;

    @Min(1)
    private int stateId;

}
