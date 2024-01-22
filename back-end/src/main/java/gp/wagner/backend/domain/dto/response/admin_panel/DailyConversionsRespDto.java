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
public class DailyConversionsRespDto {

    // Дата заказов
    @NonNull
    private Date date;

    // Кол-во заказов. Под действиями подразумевается как заказ, так и добавление в корзину
    @NonNull
    private Long actionsCount;

    // Кол-во визитов
    @NonNull
    private Long visitsCount;

    // Конверсия в заказ
    @NonNull
    private Float cvr;

    public DailyConversionsRespDto(Object[] rawTuple) {
        this.date = (Date) rawTuple[0];
        this.actionsCount = (Long) rawTuple[1];
        this.visitsCount = (Long) rawTuple[2];
        this.cvr = ((BigDecimal) rawTuple[3]).floatValue();
    }
}
