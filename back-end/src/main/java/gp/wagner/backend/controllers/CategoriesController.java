package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.CategoryRequestDto;
import gp.wagner.backend.domain.dto.response.categories.CategoryBreadcrumbsDto;
import gp.wagner.backend.domain.dto.response.categories.CategoryDto;
import gp.wagner.backend.domain.dto.response.categories.CategoryDtoWithChildren;
import gp.wagner.backend.domain.dto.response.category_views.CategoriesViewsDtoContainer;
import gp.wagner.backend.domain.dto.response.category_views.CategoriesViewsWithChildrenDto;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.categories.RepeatingCategory;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.visits.CategoryViews;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.ControllerUtils;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.middleware.Services;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;


@RestController
@RequestMapping(value = "/api/categories")
public class CategoriesController {

    //Выборка всех категорий
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CategoryDto> getCategories() {

        List<Category> categories = Services.categoriesService.getAll();

        //Создаём из вариантов товаров список объектов DTO для вариантов товаров
        return categories.stream().map(CategoryDto::factory).toList();
    }

    // Выборка всех категорий с обобщением повторяющихся - для главной страницы с плиткой категорий
    @GetMapping(value = "/get_all_with_repeating", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<CategoryDto> getCategoriesWithRepeatingOnes() {

        List<Category> categories = Services.categoriesService.getAll();

        Map<Long, CategoryDto> categoryDtosMap = new HashMap<>();

        // Добавить в map категории с обобщёнными повторяющимися категориям
        categories.forEach(c -> CategoryDto.factory(c, categoryDtosMap));

        //Создаём из вариантов товаров список объектов DTO для вариантов товаров
        return categoryDtosMap.values();
    }

    // Выборка всех категорий с дочерними категориями. Для построения дерева
    @GetMapping(value = "/get_tree", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<CategoryDtoWithChildren> getCategoriesWithChildren() {

        List<Category> parentCategories = Services.categoriesService.getAllParentCategories();

        List<CategoryDtoWithChildren> categoriesWithChildren = new ArrayList<>();

        for (Category category : parentCategories) {

            // Dto без подсчёта кол-ва товаров каждой категории
            CategoryDtoWithChildren categoryWithChildren = CategoryDtoWithChildren.factory(category);

            // Рекурсивно выбрать дочерние категории
            categoryWithChildren.setChildCategories(ControllerUtils.findChildCategories(category.getId()));
            categoriesWithChildren.add(categoryWithChildren);

        }

        //Создаём из вариантов товаров список объектов DTO для вариантов товаров
        return categoriesWithChildren;
    }

    //Добавление категории
    @PostMapping()
    public ResponseEntity<Long> createCategory(@RequestPart(value = "category_name") String categoryName,
                                               @RequestPart(value = "parent_id") @Nullable Long parentId) {

        if (categoryName.isEmpty() || categoryName.isBlank())
            throw new ApiException("Название категории не может быть пустым!");

        if (!Utils.checkCharset(categoryName, StandardCharsets.UTF_8))
            categoryName = new String(categoryName.getBytes(StandardCharsets.UTF_8));

        //long createdCategoryId = Services.categoriesService.createAndCheckRepeating(categoryName, parentId);
        long createdCategoryId = Services.categoriesService.createAndCheckRepeating(categoryName, parentId);

        return ResponseEntity.ok(createdCategoryId);
    }


    //Изменение категории
    @PutMapping(value = "/update")
    public ResponseEntity<CategoryDto> updateCategory(@Valid @RequestBody CategoryRequestDto dto) {

        Category upatedCategory = Services.categoriesService.updateAndCheckRepeating(dto);

        return ResponseEntity.ok(CategoryDto.factory(upatedCategory));
    }

    //Получить просмотры каждой категории
    @GetMapping(value = "/category_views/{category_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CategoriesViewsDtoContainer getCategoryViews(@PathVariable long category_id) {

        SimpleTuple<SimpleTuple<Boolean, Category>, List<CategoryViews>> categoryViewsTuple = Services.categoryViewsService.getByCategoryId(category_id);

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
    public List<CategoriesViewsWithChildrenDto> getAllCategoryViews() {

        //Получить все категории, где не указана родительская категория. То есть выборка родительских категорий (корней).
        List<Category> parentCategories = Services.categoriesService.getAllParentCategories();

        //Все дочерние категории на одном уровне рекурсии
        List<Long> childCategories;

        //Список DTO
        List<CategoriesViewsWithChildrenDto> dtoList = new ArrayList<>();

        //Пройти по всем начальным родительским категориям
        for (Category c : parentCategories) {
            childCategories = Services.categoriesService.getChildCategoriesIds(c.getId());

            //Создать dto с подсчётом просмотров текущей и дочерних категорий на всех уровнях
            CategoriesViewsWithChildrenDto categoriesViewsDto = new CategoriesViewsWithChildrenDto(
                    Services.categoryViewsService.getSimpleCVByCategoryId(c.getId()));

            //Вложенный список, понятно, что не самое эффективное решение, но пока не понятно, как по-другому можно пройти по дочерним элементам
            for (long id : childCategories) {
                categoriesViewsDto.childCategories.add(ControllerUtils.findSubCategoryViews(id));
            }

            dtoList.add(categoriesViewsDto);

            //Очистить
            childCategories.clear();

        }

        return dtoList;
    }

    // Скрыть категорию (данный endpoint относится к admin-панели и требует быть либо админом, либо модератором)
    @PutMapping(value = "/hide_category/{category_id}")
    public ResponseEntity<Boolean> hideCategoryById(@Valid @PathVariable(value = "category_id") @Min(0) long categoryId) {

        Services.categoriesService.hideById(categoryId);

        return ResponseEntity.ok()
                .body(true);
    }

    // Восстановить категорию из скрытия
    @PutMapping(value = "/recover_hidden_category")
    public ResponseEntity<Boolean> recoverHiddenCategoryById(@Valid @RequestParam(value = "category_id") @Min(0) long categoryId,
                                                             @RequestParam(value = "recover_heirs", defaultValue = "true") boolean recoverHeirs) {

        Services.categoriesService.recoverHiddenById(categoryId, recoverHeirs);

        return ResponseEntity.ok()
                .body(true);

    }


    // Выборка breadcrumbs для заданной категории
    @GetMapping(value = "/breadcrumbs/by_category/{category_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CategoryBreadcrumbsDto getBreadcrumbsByCategory(@PathVariable(value = "category_id") long categoryId) {

        // Если задан id повторяющейся категории, то будет задан только 1 уровень breadcrumbs
        if (categoryId < 0) {
            RepeatingCategory repeatingCategory = Services.categoriesService.getRepeatingCategoryById(Math.abs(categoryId));
            return new CategoryBreadcrumbsDto(repeatingCategory);
        }

        Category category = Services.categoriesService.getById(categoryId);

        return ControllerUtils.createBreadCrumbByCategory(category);
    }
    // Выборка breadcrumbs для заданного товара
    @GetMapping(value = "/breadcrumbs/by_product/{product_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CategoryBreadcrumbsDto getBreadcrumbsByProduct(@PathVariable(value = "product_id") long product_id) {

        Product product = Services.productsService.getById(product_id);

        return ControllerUtils.createBreadCrumbByCategory(product.getCategory());
    }
}
