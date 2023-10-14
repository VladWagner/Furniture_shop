package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.dto.request.crud.AttributeValueDto;
import gp.wagner.backend.domain.entites.eav.AttributeValue;

import java.util.List;


public interface AttributeValuesService {

    //Добавление записи
    public void save(AttributeValue attributeValue);
    public void save(long productId,AttributeValueDto dto);

    //Изменение записи
    public void update(AttributeValue attributeValue);
    public void update(AttributeValueDto dto);

    //Выборка всех записей
    public List<AttributeValue> getAll();

    //Удаление записей по списку идентификаторов
    void deleteByIdList(List<Long> idList);


    //Выборка записи под id
    public AttributeValue getById(Long id);

    //Получение значений атрибутов конкретного товара
    public List<AttributeValue> getValuesByProductId(long product_id);

}
