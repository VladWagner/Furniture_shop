package gp.wagner.backend.domain.dto.response.admin_panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Tuple;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

// DTO для передачи статистики заказов по дням
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyOrdersStatisticsRespDto {

    // Дата
    @NotNull
    private Date date;

    // Кол-во заказов
    @NotNull
    @JsonProperty("orders_count")
    private Long ordersCount;

    // Общее кол-во посещений интернет-магазина
    @NotNull
    @JsonProperty("visits_count")
    private Integer visitsCount;

    // Конверсия из посещения в заказ
    @NotNull
    private Float cvr;

    // Суммы всех заказов
    @NotNull
    @JsonProperty("orders_sums")
    private Long ordersSums;

    public DailyOrdersStatisticsRespDto(Tuple rawTuple) {
        this.date = rawTuple.get(0, Date.class);
        this.ordersCount = rawTuple.get(1, Long.class);
        this.visitsCount = rawTuple.get(2, BigDecimal.class).intValue();
        this.cvr = rawTuple.get(3, BigDecimal.class).floatValue();
        this.ordersSums = (rawTuple.get(4, BigDecimal.class)).longValue();
    }
}
