package gp.wagner.backend.domain.dto.request.crud;

import gp.wagner.backend.validation.order_request_dto.annotations.ValidOrderRequestDto;
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
@ValidOrderRequestDto
public class OrderRequestDto {

    //Для редактирования заказа
    @Nullable
    private Long id;

    @NotNull
    private Map<Integer, Integer> productVariantIdAndCount;

    // Объект покупателя (может создаваться либо уже существовать) - агрегация
    @Nullable
    private CustomerRequestDto customer;

    @Nullable
    private Long code;

    @Min(1)
    private int stateId;

}
