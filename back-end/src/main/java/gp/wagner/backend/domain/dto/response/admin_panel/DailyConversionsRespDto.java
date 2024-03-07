package gp.wagner.backend.domain.dto.response.admin_panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
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
public class DailyConversionsRespDto {

    // Дата заказов
    @NotNull
    private Date date;

    // Кол-во заказов. Под действиями подразумевается как заказ, так и добавление в корзину
    @NotNull
    @JsonProperty("actions_count")
    private Long actionsCount;

    // Кол-во визитов
    @NotNull
    @JsonProperty("visits_count")
    private Long visitsCount;

    // Конверсия в заказ
    @NotNull
    private Float cvr;

    @Nullable
    private Long sums;

    public DailyConversionsRespDto(Object[] rawTuple) {
        this.date = (Date) rawTuple[0];
        this.actionsCount = (Long) rawTuple[1];

        Object visitsCountObj = rawTuple[2];

        if (visitsCountObj.getClass().isAssignableFrom(BigDecimal.class))
            this.visitsCount = ((BigDecimal) visitsCountObj).longValue();
        else if (visitsCountObj.getClass().isAssignableFrom(Long.class))
            this.visitsCount = (long) visitsCountObj;

        //this.visitsCount = ((BigDecimal) rawTuple[2]).longValue();
        this.cvr = ((BigDecimal) rawTuple[3]).floatValue();
        this.sums = ((BigDecimal) rawTuple[4]).longValue();
    }

    // CTOR с получением штатного объекта Tuple
    public DailyConversionsRespDto(Tuple tuple) {
        this.date = tuple.get(0, Date.class);

        this.actionsCount = tuple.get(1, Long.class);

        Object visitsCountObj = tuple.get(2);

        if (visitsCountObj.getClass().isAssignableFrom(BigDecimal.class))
            this.visitsCount = ((BigDecimal) visitsCountObj).longValue();
        else if (visitsCountObj.getClass().isAssignableFrom(Long.class))
            this.visitsCount = (long) visitsCountObj;

        this.cvr = tuple.get(3, Number.class).floatValue();
        this.sums = tuple.get(4, Integer.class).longValue();
    }
}
