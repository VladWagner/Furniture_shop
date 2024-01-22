package gp.wagner.backend.domain.dto.request.admin_panel;

import jakarta.annotation.Nullable;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//Значение атрибута = значение характеристики варианта товара
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatesRangeAndValRequestDto extends DatesRangeRequestDto {

    // Значения здесь используются в качестве доп.данных к диапазону при выборке заказов в определённой категоии например
    @Nullable
    private Integer intValue;

    @Nullable
    private Long longValue;

    // Список дополнительных значений - статус заказа, диапазон цен и т.д.
    @Nullable
    private List<Long> longValuesList = new ArrayList<>();

}
