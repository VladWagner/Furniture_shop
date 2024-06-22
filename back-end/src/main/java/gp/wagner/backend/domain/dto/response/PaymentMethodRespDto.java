package gp.wagner.backend.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entities.orders.PaymentMethod;
import lombok.*;

//Объект для добавления производителя
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodRespDto {

    private long id;

    // Наименование способа оплаты
    @JsonProperty("method_name")
    private String methodName;

    public PaymentMethodRespDto(PaymentMethod pm) {
        this.id = pm.getId();
        this.methodName = pm.getMethodName();
    }
}
