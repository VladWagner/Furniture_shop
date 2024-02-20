package gp.wagner.backend.domain.dto.response.admin_panel;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

// DTO для передачи статистики заказов по дням
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyOrdersRespDto {

    // Дата
    @NonNull
    private Date date;

    // Кол-во заказов
    @NonNull
    private Long ordersCount;

    // Суммы всех заказов
    @NonNull
    private Long ordersSums;

    public DailyOrdersRespDto(Object[] rawTuple) {
        this.date = (Date) rawTuple[0];
        this.ordersCount = (Long) rawTuple[1];
        this.ordersSums = ((BigDecimal) rawTuple[2]).longValue();
    }
}
