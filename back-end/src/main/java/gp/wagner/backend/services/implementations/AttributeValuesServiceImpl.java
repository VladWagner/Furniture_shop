package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.crud.AttributeValueDto;
import gp.wagner.backend.domain.entites.eav.AttributeValue;
import gp.wagner.backend.repositories.AttributeValuesRepository;
import gp.wagner.backend.services.interfaces.AttributeValuesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

//Сервис для работы  с таблицей значений характеристик и её сущностью
@Service
public class AttributeValuesServiceImpl implements AttributeValuesService {

    //Репозиторий
    private AttributeValuesRepository attributeValuesRepository;

    @Autowired
    public void setAttributeValuesRepository(AttributeValuesRepository attributeValuesRepository) {
        this.attributeValuesRepository = attributeValuesRepository;
    }

    @Override
    //Добавление записи
    public void save(AttributeValue attributeValue){
        if(attributeValue != null)
            attributeValuesRepository.saveAndFlush(attributeValue);
    }

    //Сохранение характеристик для конкретного товара из DTO характеристик
    @Override
    public void save(long productId,AttributeValueDto dto) {
        if (dto == null)
            return;

        attributeValuesRepository.insertValue(productId, dto.getAttributeId(), dto.getStrValue(),
                dto.getIntValue(), dto.getFloatValue(), dto.getDoubleValue(),
                //bool значение нужно передавать  виде int, но и при этом проверять на null
                dto.getBoolValue() == null ? null : dto.getBoolValue() ? 1 : 0, dto.getDateValue());
    }

    @Override
    //Изменение записи
    public void update(AttributeValue attributeValue){
        if(attributeValue != null)
            attributeValuesRepository.saveAndFlush(attributeValue);
    }

    ///Изменение конкретной характеристики, получаем DTO
    @Override
    public void update(AttributeValueDto dto) {
        if (dto == null)
            return;

        attributeValuesRepository.updateValue(dto.getId(), dto.getStrValue(),
                dto.getIntValue(), dto.getFloatValue(), dto.getDoubleValue(),
                dto.getBoolValue() == null ? null : dto.getBoolValue() ? 1 : 0, dto.getDateValue());
    }

    public void delete(AttributeValue attributeValue) {
        if (attributeValue != null)
            attributeValuesRepository.delete(attributeValue);
    }

    public void deleteById(Long id) {
        if (id != null)
            attributeValuesRepository.deleteById(id);
    }

    @Override
    //Выборка всех записей
    public List<AttributeValue> getAll(){return attributeValuesRepository.findAll();}

    //Удаление списка характеристик
    @Override
    public void deleteByIdList(List<Long> idList) {
        if (idList == null)
            return;
        attributeValuesRepository.deleteByIdIn(idList);
    }


    @Override
    //Выборка записи по id
    public AttributeValue getById(Long id){
        if (id != null)
            return attributeValuesRepository.findById(id).get();
        return null;
    }

    //Получение значений характеристик для конкретного товара
    @Override
    public List<AttributeValue> getValuesByProductId(long product_id) {
        return attributeValuesRepository.findAttributeValuesByProductId(product_id);
    }

}
