package gp.wagner.backend.domain.dto.request.admin_panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

//Значение атрибута = значение характеристики варианта товара
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatesRangeAndValRequestDto extends DatesRangeRequestDto {

    // Значения здесь используются в качестве доп.данных к диапазону при выборке заказов в определённой категоии например
    @Nullable
    @JsonProperty("int_value")
    private Integer intValue;

    @Nullable
    @JsonProperty("long_value")
    private Long longValue;

    // Список дополнительных значений - статус заказа, диапазон цен и т.д.
    @Nullable
    @JsonProperty("long_values_list")
    private List<Long> longValuesList = new ArrayList<>();

    // Словарь с названием параметра и его значением ("status_id" : 1)
    @Nullable
    @JsonProperty("additional_values_map")
    private Map<String, ?> additionalValues;

}