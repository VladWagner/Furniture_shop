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
public class DiscountNotFound implements Supplier<ApiException> {

    // id скидки, по которой осуществлялся поиск
    private Long discountId;

    // Значение % скидки по которому мог происходить поиск
    private Float percentage;

    public DiscountNotFound(Long discountId) {
        this.discountId = discountId;
    }

    public DiscountNotFound(Float percentage) {
        this.percentage = percentage;
    }

    @Override
    public ApiException get() {

        StringBuilder sb = new StringBuilder("Скидка ");

        if ( this.discountId != null)
            sb.append(String.format("с id %d ", this.discountId));

        // Если задан и id скидки и %
        if (this.percentage != null)
            sb.append(String.format("и процентом %f.2 ", percentage));

        return new ApiException(sb.append("не найден!").toString());
    }
}
