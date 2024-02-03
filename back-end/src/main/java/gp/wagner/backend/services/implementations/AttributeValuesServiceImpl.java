package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.crud.AttributeValueDto;
import gp.wagner.backend.domain.dto.response.filters.FilterValuesDto;
import gp.wagner.backend.domain.entites.eav.AttributeValue;
import gp.wagner.backend.infrastructure.ServicesUtils;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.AttributeValuesRepository;
import gp.wagner.backend.services.interfaces.AttributeValuesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
            return attributeValuesRepository.findById(id).orElse(null);
        return null;
    }

    //Получение значений характеристик для конкретного товара
    @Override
    public List<AttributeValue> getValuesByProductId(long product_id) {
        return attributeValuesRepository.findAttributeValuesByProductId(product_id);
    }

    // Сформировать список объектов для формирования фильтра
    private List<FilterValuesDto<Integer>> createDtoList(List<Object[]> rawResult){
        List<FilterValuesDto<Integer>> dtoList = new LinkedList<>();

        // Распарсить результирующий набор
        for (Object[] result: rawResult) {

            FilterValuesDto<Integer> filterDto = new FilterValuesDto<>();

            filterDto.setAttributeId((int)      result[0]);
            filterDto.setAttributeName((String) result[1]);
            filterDto.setPriority((Float)       result[2]);
            filterDto.setMin((Integer)          result[3]);
            filterDto.setMax((Integer)          result[4]);
            filterDto.setValue((String)         result[5]);

            dtoList.add(filterDto);

        }

        return dtoList;
    }


    // Получить значения характеристик
    @Override
    public Map<String, List<FilterValuesDto<Integer>>> getFiltersValuesByCategory(long categoryId) {

        List<Object[]> resultSet = attributeValuesRepository.getAttributeValuesByCategory(categoryId);

        List<FilterValuesDto<Integer>> dtoList = createDtoList(resultSet);

        // Добавить список производителей
        dtoList.addAll(Services.producersService.getProducersByCategory(categoryId)
                .stream()
                .map(element -> new FilterValuesDto<Integer>(null,"Производители", element.getProducerName(), null, null))
                .toList());

        // Добавить диапазон цен ()
        FilterValuesDto<Integer> pricesRange = Services.productsService.getPricesRangeInCategory(categoryId);

        if (pricesRange != null && pricesRange.getMin() != null)
            dtoList.add(pricesRange);

        // Группировка по названию характеристики
        Map<String, List<FilterValuesDto<Integer>>> groupedFilters = dtoList.stream()
                                                                    .sorted(Comparator.comparing(FilterValuesDto::getPriority))
                                                                    .collect(Collectors.groupingBy(FilterValuesDto::getAttributeName));


        return ServicesUtils.createAndSortFilterMap(dtoList);
    }

    // Получить те же значения для фильтрации из списка id категорий
    @Override
    public java.util.Map<String, List<FilterValuesDto<Integer>>> getFiltersValuesByCategories(List<Long> categoriesIds) {
        List<Object[]> resultSet = attributeValuesRepository.getAttributeValuesByCategories(categoriesIds);

        List<FilterValuesDto<Integer>> dtoList = createDtoList(resultSet);

        // Добавить диапазон цен по конкретным категориям
        FilterValuesDto<Integer> pricesRange = Services.productsService.getPricesRangeInCategories(categoriesIds);

        if (pricesRange != null && pricesRange.getMin() != null)
            dtoList.add(pricesRange);

        // Список производителей
        dtoList.addAll(Services.producersService.getProducersInCategories(categoriesIds)
                .stream()
                .map(element -> new FilterValuesDto<Integer>(0,"Producers",element.getProducerName(), null, null))
                .toList());

        return ServicesUtils.createAndSortFilterMap(dtoList);
    }

    //Можно так же ещё сделать выборку значений фильтраци по ключевому слову.
    // То есть искать во всех репозитория так же и по ключевому слову в товаре
    @Override
    public java.util.Map<String, List<FilterValuesDto<Integer>>> getFiltersValuesByKeyword(String keyword) {

        List<FilterValuesDto<Integer>> dtoList = createDtoList(attributeValuesRepository.getAttributeValuesByKeyword(keyword));

        // Добавить диапазон цен по конкретным категориям
        FilterValuesDto<Integer> pricesRange = Services.productsService.getPricesRangeByKeyword(keyword);

        if (pricesRange != null && pricesRange.getMin() != null)
            dtoList.add(pricesRange);

        // Список производителей
        dtoList.addAll(Services.producersService.getProducersByProductKeyword(keyword)
                .stream()
                .map(element -> new FilterValuesDto<Integer>(0,"Producers",element.getProducerName(), null, null))
                .toList());

        return ServicesUtils.createAndSortFilterMap(dtoList);
    }
}
