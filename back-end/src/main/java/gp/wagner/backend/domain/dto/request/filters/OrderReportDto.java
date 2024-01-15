package gp.wagner.backend.domain.dto.request.filters;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.sql.Date;

//Объект для добавления/редактирования заказа
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderReportDto {

    // Заказ по определённому id
    @Nullable
    private Long id;

    // Заказы по списку вариантов товаров
    @Nullable
    private Long[] productVariantsId;

    // Заказы по определённому варианту товара
    @Nullable
    private Long productVariantId;

    // Заказы по определённому товару
    @Nullable
    private Long productId;

    // Заказы для нескольких покупателей
    @Nullable
    private Long[] customersId;

    // Заказы по определённому покупателю
    @Nullable
    private Long customerId;

    // Заказы по конкретному номеру телефона
    @Nullable
    private String customerPhoneNumber;

    // Заказы по конкретному email
    @Nullable
    private String customerEmail;

    // Для выборки заказа по коду
    @Nullable
    private Long code;

    // Для выборки заказов в определённом состоянии
    @Min(1)
    @Nullable
    private Integer stateId;

    // Минимальная дата заказа
    @Nullable
    private Date minDate;

    // Максимальная дата заказа
    @Nullable
    private Date maxDate;

}
