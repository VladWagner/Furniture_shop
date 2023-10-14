package gp.wagner.backend.domain.dto.request.filters;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

//Здесь создаётся и хранится список фильтров, их операция сравнения между собой и сравнения с другими блоками
@Data
@Getter
@Setter
public class ProductFilterBlock {

    //Внешнее условие - объедения с другими блоками (возможно излишнее, поскольку использовать практически нереально - )
    //Например имеется блок с условиями или (Каркас деревянный или металлический) -> он соединяется с блоком и (есть раскладушка и уголок)
    //Используется для задания связи между спецификациями (ПОКА НЕТ и вороятнее всего не будет)
    @Nullable
    private String outerCondition;

    //region Really implementing
    //Внутреннее условие - объединения фильтров внутри блока (И/ИЛИ)
    @NotNull
    private String innerRule;

    @NotNull
    private List<ProductFilterDto> productFilters;
    //endregion

}
