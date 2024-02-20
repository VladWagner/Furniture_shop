package gp.wagner.backend.domain.dto.response.admin_panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

// DTO для передачи статистики заказов по дням
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViewsFrequencyRespDto {

    // id категории
    @NonNull
    private Integer category_id;

    // Имя категории
    @NonNull
    @JsonProperty(value = "category_name")
    private String categoryName;

    // Id родительской категории
    @NonNull
    @JsonProperty(value = "parent_category")
    private Integer parentId;

    // Сумма просмотров
    @NonNull
    @JsonProperty(value = "views_sum")
    private Integer viewsSum;

    // Количество уникальных посетителей
    @NonNull
    @JsonProperty(value = "visitors_count")
    private Long visitorsCount;

    // Частота
    @NonNull
    private Float frequency;

    public ViewsFrequencyRespDto(Object[] rawTuple) {
        category_id = (Integer) rawTuple[0];
        categoryName = rawTuple[1].toString();
        parentId = (Integer) rawTuple[2];
        viewsSum = ((BigDecimal) rawTuple[3]).intValue();
        visitorsCount = (Long) rawTuple[4];
        frequency = ((BigDecimal) rawTuple[5]).floatValue();
    }
}
