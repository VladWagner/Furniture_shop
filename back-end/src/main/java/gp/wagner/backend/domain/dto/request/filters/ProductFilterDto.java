package gp.wagner.backend.domain.dto.request.filters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.core.SpringVersion;

//Собственно сам фильтр с id, названием атрибута и значением + операция сравнения
@Data
@Getter
@Setter
public class ProductFilterDto {

    @NotNull
    //@Min(value = 1, message = "Идентификатор характеристики не может быть <= 0")
    private Long attributeId;

    @NotNull
    private String value;

    //Логическая операция ><=!= для заданного атрибута
    @NotNull
    private String operation;

    @Nullable
    private String attributeName = null;

}
