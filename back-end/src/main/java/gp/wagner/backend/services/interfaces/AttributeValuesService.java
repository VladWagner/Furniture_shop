package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.dto.request.crud.AttributeValueDto;
import gp.wagner.backend.domain.dto.response.filters.FilterValuesDto;
import gp.wagner.backend.domain.entites.eav.AttributeValue;

import java.util.List;
import java.util.Map;


public interface AttributeValuesService {

    // Добавление записи
    void save(AttributeValue attributeValue);
    void save(long productId,AttributeValueDto dto);

    // Изменение записи
    void update(AttributeValue attributeValue);
    void update(AttributeValueDto dto);

    // Выборка всех записей
    List<AttributeValue> getAll();

    // Удаление записей по списку идентификаторов
    void deleteByIdList(List<Long> idList);


    // Выборка записи под id
    AttributeValue getById(Long id);

    // Получение значений атрибутов конкретного товара
    List<AttributeValue> getValuesByProductId(long product_id);

    // Получить значений атрибутов по определённой категории - для фильтров
    Map<String, List<FilterValuesDto<Integer>>> getFiltersValuesByCategory(long categoryId);

    // Получить значений атрибутов по массиву категорий
    Map<String, List<FilterValuesDto<Integer>>> getFiltersValuesByCategories(List<Long> categoriesIds);

    //Можно так же ещё сделать выборку значений фильтраци по ключевому слову.
    // То есть искать во всех репозитория так же и по ключевому слову в товаре
    Map<String, List<FilterValuesDto<Integer>>> getFiltersValuesByKeyword(String keyword);
}
