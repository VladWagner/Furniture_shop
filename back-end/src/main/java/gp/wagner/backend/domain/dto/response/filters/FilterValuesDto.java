package gp.wagner.backend.domain.dto.response.filters;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//DTO для формирования значений бокового фильтра
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FilterValuesDto<Val_type> {

    //Id характеристики
    @JsonProperty("attribute_id")
    private Integer attributeId;

    //Название характеристики
    @JsonIgnore
    private String attributeName;

    //Значение атрибута
    private String value;

    //Опциональные мин/макс значения атрибута
    @Nullable
    private Val_type min;

    @Nullable
    private Val_type max;

    // Поле для внутренней сортировки перед отправкой на фронтенд
    private Float priority = 0f;

    // CTOR, игнорирующий приоритет фильтра
    public FilterValuesDto(Integer attributeId, String attributeName, String value, Val_type min, Val_type max) {
        this.attributeId = attributeId;
        this.attributeName = attributeName;
        this.value = value;
        this.min = min;
        this.max = max;
    }

    // CTOR с приоритетом
    public FilterValuesDto(Integer attributeId, String attributeName, float priority, String value, Val_type min, Val_type max) {
        this(attributeId, attributeName, value, min, max);
        this.priority = priority;
    }

}