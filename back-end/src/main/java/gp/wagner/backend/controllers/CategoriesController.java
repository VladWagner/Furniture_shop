package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.response.CategoryDto;
import gp.wagner.backend.domain.dto.response.category_views.CategoriesViewsDtoContainer;
import gp.wagner.backend.domain.dto.response.category_views.CategoriesViewsWithChildrenDto;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.visits.CategoryViews;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.infrastructure.ControllerUtils;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.middleware.Services;
import jakarta.annotation.Nullable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/categories")
public class CategoriesController {

    //Выборка всех категорий
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CategoryDto> getCategories(){

        List<Category> categories = Services.categoriesService.getAll();

        //Создаём из вариантов товаров список объектов DTO для вариантов товаров
        return categories.stream().map(CategoryDto::factory).toList();
    }

    //Добавление категории
    @PostMapping()
    public String createCategory(@RequestPart(value = "category_name") String categoryName,
                                 @RequestPart(value = "parent_id") @Nullable Integer parentId){

        int createdCategoryId = 0;

        try {
            if (categoryName.isEmpty() || categoryName.isBlank())
                throw new ApiException("Название категории не может быть пустым!");

            if (!Utils.checkCharset(categoryName, StandardCharsets.UTF_8))
                categoryName = new String(categoryName.getBytes(StandardCharsets.UTF_8));

            createdCategoryId = (int) Services.categoriesService.createAndCheckRepeating(categoryName, parentId);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }

        return String.format("Товар с id: %d добавлен!", createdCategoryId);
    }

    //Получить просмотры каждой категории
    @GetMapping(value = "/category_views/{category_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CategoriesViewsDtoContainer/*List<CategoryViews>*/ getCategoryViews(@PathVariable long category_id){

        SimpleTuple<SimpleTuple<Boolean,Category>, List<CategoryViews>> categoryViewsTuple = Services.categoryViewsService.getByCategoryId(category_id);

        //categoryViewsTuple.getValue1().getValue2() - получить кортеж с флагом родительская категория или нет -> получить саму найденную категорию
        CategoriesViewsDtoContainer dtoContainer = new CategoriesViewsDtoContainer(categoryViewsTuple.getValue1().getValue2());

        //Если категория родительская
        if (categoryViewsTuple.getValue1().getValue1()) {
            //Получить 1-й элемент списка, поскольку он там единственный
            dtoContainer.setParentViewsCount(categoryViewsTuple.getValue2().get(0));
            return dtoContainer;
        }

        //Создаём из вариантов товаров список объектов DTO для вариантов товаров
        dtoContainer.setSubCategoriesViewsDtos(categoryViewsTuple.getValue2());
        return dtoContainer;
    }

    //Получить просмотры каждой категории
    @GetMapping(value = "/category_views/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CategoriesViewsWithChildrenDto>/*List<CategoryViews>*/ getAllCategoryViews(){

        //Получить все категории, где не указана родительская категория
        List<Category> parentCategories = Services.categoriesService.getAll()
                .stream()
                .filter(c -> c.getParentCategory() == null)
                .toList();

        //Все дочерние категории на одном уровне рекурсии
        List<Long> childCategories;

        //Список DTO
        List<CategoriesViewsWithChildrenDto> dtoList = new ArrayList<>();

        //Пройти по всем начальным родительским категориям
        for (Category c: parentCategories) {
            childCategories = Services.categoriesService.getChildCategories(c.getId().intValue());

            //Создать dto с подсчётом просмотров текущей и дочерних категорий на всех уронях
            CategoriesViewsWithChildrenDto categoriesViewsDto = new CategoriesViewsWithChildrenDto(
                    Services.categoryViewsService.getSimpleCVByCategoryId(c.getId()));

            //Вложенный список, понятно, что не самое эффективное решение, но пока не понятно, как по-другому можно пройтти по дочерним элеентам
            for (long id: childCategories) {
                categoriesViewsDto.childCategories.add(ControllerUtils.findSubCategoryViews(id));
            }

            dtoList.add(categoriesViewsDto);

            //Очистить
            childCategories.clear();

        }

        return dtoList;
    }


}
