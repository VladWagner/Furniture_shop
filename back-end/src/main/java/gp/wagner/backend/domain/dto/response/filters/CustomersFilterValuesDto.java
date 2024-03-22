package gp.wagner.backend.domain.dto.response.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

// DTO для передачи возможных значений фильтрации покупателей
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomersFilterValuesDto {


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
    private Instant minDate;

    // Максимальная дата создания записи
    @Nullable
    @JsonProperty("date_hi")
    private Instant maxDate;

    public CustomersFilterValuesDto(Tuple tuple) {
        this.minDate =              tuple.get(0, Timestamp.class).toInstant();
        this.maxDate =              tuple.get(1, Timestamp.class).toInstant();

        this.minOrdersCount =       tuple.get(2, Long.class);
        this.maxOrdersCount =       tuple.get(3, Long.class);

        this.minOrderedUnitsCount = tuple.get(4, BigDecimal.class).intValue();
        this.maxOrderedUnitsCount = tuple.get(5, BigDecimal.class).intValue();

        this.minAvgUnitPrice =      tuple.get(6, BigDecimal.class).doubleValue();
        this.maxAvgUnitPrice =      tuple.get(7, BigDecimal.class).doubleValue();

        this.minOrdersSum =         tuple.get(8, BigDecimal.class).intValue();
        this.maxOrdersSum =         tuple.get(9, BigDecimal.class).intValue();
    }
}
