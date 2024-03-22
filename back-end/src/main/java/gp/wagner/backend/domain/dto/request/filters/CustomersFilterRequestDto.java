package gp.wagner.backend.domain.dto.request.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.*;

import java.sql.Date;

// Фильтр для выборки покупателей
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomersFilterRequestDto {

    // Пользователь по определённому Id
    @Nullable
    private Long id;

    // Флаг зарегистрирован ли покупатель
    @Nullable
    @JsonProperty("is_registered")
    private Boolean isRegistered;

    // Минимальное значение кол-ва заказов
    @Nullable
    @JsonProperty("min_orders_count")
    private Long minOrdersCount;

    // Максимальное значение кол-ва заказов
    @Nullable
    @JsonProperty("max_orders_count")
    private Long maxOrdersCount;

    // Минимальное значение кол-ва заказанных товаров
    @Nullable
    @JsonProperty("min_ordered_units_count")
    private Integer minOrderedUnitsCount;

    // Максимальное значение кол-ва заказанных товаров
    @Nullable
    @JsonProperty("max_ordered_units_count")
    private Integer maxOrderedUnitsCount;

    // Минимальное значение средней цены заказываемого товара
    @Nullable
    @JsonProperty("min_avg_unit_price")
    private Double minAvgUnitPrice;

    // Максимальное значение средней цены заказываемого товара
    @Nullable
    @JsonProperty("max_orders_sum")
    private Double maxAvgUnitPrice;

    // Минимальное значение суммы всех заказов
    @Nullable
    @JsonProperty("min_orders_sum")
    private Integer minOrdersSum;

    // Максимальное значение суммы всех заказов
    @Nullable
    @JsonProperty("max_avg_unit_price")
    private Integer maxOrdersSum;

    // Минимальная создания
    @Nullable
    @JsonProperty("date_lo")
    private Date minDate;

    // Максимальная дата создания записи
    @Nullable
    @JsonProperty("date_hi")
    private Date maxDate;

}
