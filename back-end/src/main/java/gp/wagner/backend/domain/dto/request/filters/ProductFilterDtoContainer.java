package gp.wagner.backend.domain.dto.request.filters;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

//Контейнер блоков фильтров и самих фильтров
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterDtoContainer {


    //region Temporary unused
    //productFilterDtoListAnd и productFilterDtoListOr - запасной вариант и по сути рудименты, поскольку все фильтра объединяются внутри блока
    //и имеют заданный тип связи между собой (и/или) + тип связи между блоками фильтров
    //Это рудименты, пока их можно оставить, поскольку они используются в невызываемом методе в Specifications
    @NotNull
    private List<ProductFilterDto> productFilterDtoListAnd;
    @NotNull
    private List<ProductFilterDto> productFilterDtoListOr;
    //endregion

    //Для динамического создания блоков условий И/ИЛИ можно создать ещё одно поле - список объектов, в которых будет задано внешнее условие (И/ИЛИ)
    //и внутреннее условие для каждого элемента, тогда можно будет понять при чтении данного списка, как будет присоединятся этот блок условий
    //через И/ИЛИ и как условия будут объединяться внутри

    @NotNull
    private List<ProductFilterBlock> productFilterBlockList;

}
