package gp.wagner.backend.domain.dto.response.filters;


import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.aspectj.weaver.ast.Instanceof;

//DTO для формирования значений бокового фильтра
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FilterValueDto<Val_type> {

    //Id характеристики
    private Integer attributeId;

    //Название характеристики
    private String attributeName;

    //Значение атрибута
    private String value;

    //Опциональные мин/макс значения атрибута
    @Nullable
    private Val_type min;

    @Nullable
    private Val_type max;

}
