package gp.wagner.backend.domain.exceptions.suppliers;

import gp.wagner.backend.domain.exceptions.classes.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Supplier;

// Данный класс нужен, чтобы постоянно не писать сообщения в supplier в методах Optional<>.ElseThrow()
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderNotFound implements Supplier<ApiException> {

    // id скидки, по которой осуществлялся поиск
    private Long orderId;

    // Код по которому мог осуществляться поиск
    private Long code;

    // Email покупателя по которому мог осуществляться поиск
    private String email;

    public OrderNotFound(Long orderId, Long orderCode) {
        this.orderId = orderId;
        this.code  = orderCode;
    }

    public OrderNotFound(String email) {
        this.email = email;
    }

    @Override
    public ApiException get() {

        StringBuilder sb = new StringBuilder("Заказ ");

        boolean idNotNull = orderId != null;
        boolean codeNotNull = code != null;

        if (idNotNull)
            sb.append(String.format("с id %d ", orderId));

        // Если задан и id заказа и код
        if (codeNotNull)
            sb.append(String.format("%2$s кодом %1$s ", code, idNotNull ? "и" : "с"));

        // Если задан и id пользователя и логин и ещё email, тогда добавить союз «и»
        if (email != null)
            sb.append(String.format("%2$s email'ом покупателя %1$s ", email, idNotNull || codeNotNull ? "и" : "c"));

        return new ApiException(sb.append("не найден!").toString());
    }
}
