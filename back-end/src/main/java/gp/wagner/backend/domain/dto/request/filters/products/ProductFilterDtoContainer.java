package gp.wagner.backend.domain.dto.request.filters.products;

import jakarta.annotation.Nullable;
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


    //Для динамического создания блоков условий И/ИЛИ можно создать ещё одно поле - список объектов, в которых будет задано внешнее условие (И/ИЛИ)
    //и внутреннее условие для каждого элемента, тогда можно будет понять при чтении данного списка, как будет присоединятся этот блок условий
    //через И/ИЛИ и как условия будут объединяться внутри

    //@NotNull
    @Nullable
    private List<ProductFilterBlock> productFilterBlockList;

    //Список заданных производителей
    @Nullable
    private List<String> producersNames;

}
