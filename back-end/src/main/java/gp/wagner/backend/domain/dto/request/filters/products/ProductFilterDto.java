package gp.wagner.backend.domain.dto.request.filters.products;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

//Собственно сам фильтр с id, названием атрибута и значением + операция сравнения
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
