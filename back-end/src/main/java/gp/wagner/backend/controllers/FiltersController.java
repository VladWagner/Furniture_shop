package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.dto.response.filters.FilterValuesDto;
import gp.wagner.backend.domain.dto.response.filters.UserFilterValuesDto;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.enums.ProductsOrVariantsEnum;
import gp.wagner.backend.middleware.Services;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/api/filter")
public class FiltersController {

    //Выборка всех корзин заданного пользователя
    /**
     * <p>Здесь получается, что отправляется словарь состоящий из строки названия [характеристика, List<dto>]</p>
     * <p>Следовательно на фронте нужно будет проверять длину списка у каждого ключа и если она == 1 и при этом value == null, тогда это диапазон</p>
     * <p>Если же длина списка у ключа > 1, тогда это значения для чек-боксов и здесь нужно получать именно value. В таком случае нужно
     * проверять, что value != null, в противном случае просто игнорировать данный DTO</p>
     * */
    @GetMapping(value = "/filter_by_category/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<FilterValuesDto<Integer>>> getFilterByCategory(@PathVariable long id){

        Map<String, List<FilterValuesDto<Integer>>> filterValueDtoList = Services.attributeValuesService.getFiltersValuesByCategory(id);

        if (filterValueDtoList == null)
            throw new ApiException(String.format("Значения фильтрации для категории %d не найдены. Not found!", id));

        return  filterValueDtoList;
    }

    // Подсчёт кол-ва товаров в текущем состоянии фильтра
    @GetMapping(value = "/count_by_filter", produces = MediaType.APPLICATION_JSON_VALUE)
    public long countProductsByFilter(
            @Valid @RequestPart(value = "filter") ProductFilterDtoContainer filtersContainer,
            @RequestParam(value = "category_id", defaultValue = "0")  Long categoryId,
            @RequestParam(value = "price_range", defaultValue = "") String priceRange
    ){

        long result = Services.productsService.countData(filtersContainer, categoryId < 1 ? null : categoryId,
                priceRange.isEmpty() ? null : priceRange, ProductsOrVariantsEnum.PRODUCTS);

        return result;
    }

    // Выборка значений для блока фильтров при поиске по id категорий найденных товаров
    // Принимаем map, поскольку иначе неудавалось передать именованную коллекцию
    @GetMapping(value = "/filter_by_categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<FilterValuesDto<Integer>>> getFilterByCategories(@RequestPart(value = "categories") Map<String, Long[]> ids){

        // Получение списка id категорий
        List<Long> idList = Arrays.stream(ids.values().stream().toList().get(0)).toList();

        Map<String, List<FilterValuesDto<Integer>>> filterValueDtoList = Services.attributeValuesService.getFiltersValuesByCategories(idList);

        if (filterValueDtoList == null)
            throw new ApiException("Значения фильтрации для в заданных категориях не найдены. Not found!");

        return  filterValueDtoList;
    }

    //Выборка значений для блока фильтров при поиске по ключевому слову (если товары по нему были найдены)
    @GetMapping(value = "/filter_for_keyword", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<FilterValuesDto<Integer>>> getFilterByKeyword(@RequestParam(value = "key") String key){

        Map<String, List<FilterValuesDto<Integer>>> filterValueDtoList = Services.attributeValuesService.getFiltersValuesByKeyword(key.toLowerCase());

        if (filterValueDtoList == null)
            throw new ApiException(String.format("Значения фильтрации по ключевому слову '%s' найдены не были. Not found!", key.length() > 10 ? key.substring(0,10).trim() : key));

        return  filterValueDtoList;
    }

    // Возврат фильтра для пользователей
    @GetMapping(value = "/filter_for_users", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserFilterValuesDto getFilterForUsers(){

        UserFilterValuesDto filterValueDtoList = Services.usersService.getFilterValues();

        if (filterValueDtoList == null)
            throw new ApiException("Значения для фильтрации пользователей не найдены!");

        return  filterValueDtoList;
    }

}
