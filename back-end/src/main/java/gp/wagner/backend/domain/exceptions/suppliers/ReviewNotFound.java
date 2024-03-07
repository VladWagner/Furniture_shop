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
public class ReviewNotFound implements Supplier<ApiException> {

    // id отзыва, по котором осуществлялся поиск
    private Long reviewId;

    // id пользователя по которому мог осуществляться поиск
    private Long userId;

    // id товара
    private Long productId;


    @Override
    public ApiException get() {

        StringBuilder sb = new StringBuilder("Отзыв ");

        boolean reviewNotNull = this.reviewId != null;
        boolean productNotNull = this.productId != null;

        if (reviewNotNull)
            sb.append(String.format("с id %d ", this.reviewId));

        // Если задан и id товара
        if (productNotNull)
            sb.append(String.format("%s товара с id %d ", reviewNotNull ? "и" : "для", productId));

        // Если задан и id товара
        if (userId != null)
            sb.append(String.format("%s пользователя с id %d ", reviewNotNull || productNotNull ? "и" : "от", userId));

        return new ApiException(sb.append("не найден!").toString());
    }
}
