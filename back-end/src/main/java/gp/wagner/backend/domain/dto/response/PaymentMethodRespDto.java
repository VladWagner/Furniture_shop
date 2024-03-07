package gp.wagner.backend.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.orders.PaymentMethod;
import gp.wagner.backend.domain.entites.products.Producer;
import lombok.*;

import java.util.Date;

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
