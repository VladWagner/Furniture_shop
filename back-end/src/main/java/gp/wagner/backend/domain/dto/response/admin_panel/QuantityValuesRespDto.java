package gp.wagner.backend.domain.dto.response.admin_panel;

import jakarta.persistence.Tuple;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

// DTO для передачи статистики заказов по дням
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuantityValuesRespDto {

    // Минимальные значения
    @NonNull
    private Float min;

    // Средние значения
    @NonNull
    private Double avg;

    // Максимальные значения
    @NonNull
    private Float max;

    public QuantityValuesRespDto(Object[] rawTuple) {

        this.min = ((BigDecimal) rawTuple[0]).floatValue();
        this.avg = ((BigDecimal) rawTuple[1]).doubleValue();
        this.max = ((BigDecimal) rawTuple[2]).floatValue();

    }

    public QuantityValuesRespDto(Tuple tuple) {

        this.min = tuple.get(0, Integer.class).floatValue();
        this.avg = tuple.get(1, BigDecimal.class).doubleValue();
        this.max = tuple.get(2, Integer.class).floatValue();

    }

}
