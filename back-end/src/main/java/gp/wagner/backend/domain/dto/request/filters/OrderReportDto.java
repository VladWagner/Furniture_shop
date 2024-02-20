package gp.wagner.backend.domain.dto.request.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.Date;


//Объект для задания параметров выборки статистики
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
    @JsonProperty("product_variants_id")
    private Long[] productVariantsId;

    // Заказы по определённому варианту товара
    @Nullable
    @JsonProperty("product_variant_id")
    private Long productVariantId;

    // Заказы по определённому товару
    @Nullable
    @JsonProperty("product_id")
    private Long productId;

    // Заказы для нескольких покупателей
    @Nullable
    @JsonProperty("customers_id")
    private Long[] customersId;

    // Заказы по определённому покупателю
    @Nullable
    @JsonProperty("customer_id")
    private Long customerId;

    // Заказы по конкретному номеру телефона
    @Nullable
    @JsonProperty("customer_phone_number")
    private String customerPhoneNumber;

    // Заказы по конкретному email
    @Nullable
    @JsonProperty("customer_email")
    private String customerEmail;

    // Для выборки заказа по коду
    @Nullable
    private Long code;

    // Для выборки заказов в определённом состоянии
    @Nullable
    @JsonProperty("state_id")
    private Integer stateId;

    // Минимальная дата заказа
    @Nullable
    @JsonProperty("min_date")
    private Date minDate;

    // Максимальная дата заказа
    @Nullable
    @JsonProperty("max_date")
    private Date maxDate;

}
