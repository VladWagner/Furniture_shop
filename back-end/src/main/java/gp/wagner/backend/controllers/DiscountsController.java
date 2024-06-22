package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.DiscountRequestDto;
import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.dto.response.categories.CategoryDtoWithChildren;
import gp.wagner.backend.domain.dto.response.discounts.DiscountDetailedRespDto;
import gp.wagner.backend.domain.dto.response.discounts.DiscountRespDto;
import gp.wagner.backend.domain.dto.response.products.ProductPreviewRespDto;
import gp.wagner.backend.domain.entities.categories.Category;
import gp.wagner.backend.domain.entities.products.Discount;
import gp.wagner.backend.domain.entities.products.Product;
import gp.wagner.backend.domain.entities.products.ProductVariant;
import gp.wagner.backend.infrastructure.ControllerUtils;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.infrastructure.enums.ProductsOrVariantsEnum;
import gp.wagner.backend.infrastructure.enums.sorting.DiscountsSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.ProductsSortEnum;
import gp.wagner.backend.middleware.Services;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/api/discounts")
public class DiscountsController {

    //Выборка всех скидок
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<DiscountRespDto> getAllDiscounts(@Valid @RequestParam(value = "offset") @Max(100) int pageNum,
                                                    @Valid @RequestParam(value = "limit") @Max(80) int limit,
                                                    @RequestParam(value = "sort_by", defaultValue = "id")  String sortBy,
                                                    @RequestParam(value = "sort_type", defaultValue = "asc") String sortType) {

        Page<Discount> discountPaged = Services.discountsService.getAll(pageNum, limit,
                DiscountsSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(
                discountPaged, () -> discountPaged.getContent().stream().map(DiscountRespDto::new).toList()
        );
    }

    // Выборка скидки по id
    @GetMapping(value = "/by_id/{discount_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DiscountDetailedRespDto> getById(@Valid @PathVariable(value = "discount_id") @Min(0) long discountId) {

        Discount foundDiscount = Services.discountsService.getById(discountId);


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new DiscountDetailedRespDto(foundDiscount));
    }

    // Создание скидки
    @PostMapping()
    public ResponseEntity<DiscountRespDto> createDiscount(@Valid @RequestPart(value = "discount") DiscountRequestDto dto) {

        Discount createdDiscount = Services.discountsService.create(dto);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new DiscountRespDto(createdDiscount));
    }

    // Изменение скидки
    @PutMapping(value = "/update_discount")
    public ResponseEntity<DiscountRespDto> updateDiscounts(@Valid @RequestPart(value = "discount") DiscountRequestDto dto) {

        Discount updatedDiscount = Services.discountsService.update(dto);


        return ResponseEntity
                .status(HttpStatus.OK)
                .body((new DiscountRespDto(updatedDiscount)));

    }


    // Деактивация скидки
    @PutMapping(value = "/deactivate/{discount_id}")
    public ResponseEntity<String> deactivateDiscount(@Valid @PathVariable(value = "discount_id") @Min(0) long discountId) {

        Services.discountsService.deactivateById(discountId, null);

        Discount deactivatedDiscount = Services.discountsService.getById(discountId);

        return ResponseEntity
                .status(!deactivatedDiscount.getIsActive() ? HttpStatus.OK : HttpStatus.NOT_MODIFIED)
                .body(String.format("Скидка с id: %d %s деактивирована", deactivatedDiscount.getId(),
                        deactivatedDiscount.getIsActive() ? "не была" : "успешно"));


    }// deleteProducer

    // активация скидки
    @PutMapping(value = "/activate/{discount_id}")
    public ResponseEntity<String> activateDiscount(@Valid @PathVariable(value = "discount_id") @Min(0) long discountId) {

        Services.discountsService.activateById(discountId);

        Discount deactivatedDiscount = Services.discountsService.getById(discountId);

        return ResponseEntity
                .status(deactivatedDiscount.getIsActive() ? HttpStatus.OK : HttpStatus.NOT_MODIFIED)
                .body(String.format("Скидка с id: %d %s активирована", deactivatedDiscount.getId(),
                        deactivatedDiscount.getIsActive() ?"успешно" : "не была"));


    }

    // Выборка всех товаров по категории с пагинацией
    @GetMapping(value = "/products_by_category",produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ProductPreviewRespDto> getProductsByCategoryAndPricePaged(
            @RequestParam(value = "category_id", defaultValue = "1") long categoryId,
            @Valid @RequestParam(value = "offset", defaultValue = "1") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit", defaultValue = "20") @Max(50) int limit,
            @RequestParam(value = "price_range", defaultValue = "") String priceRange,
            @RequestParam(value = "sort_by", defaultValue = "id")  String sortBy,
            @RequestParam(value = "sort_type", defaultValue = "asc") String sortType){

        Page<Product> productsPage = Services.productsService.getByCategoryAndPrice(categoryId, priceRange,pageNum, limit,
                ProductsSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType), ProductsOrVariantsEnum.VARIANTS);

        // Получить кортеж диапазона цен для формирования DTO
        SimpleTuple<Integer, Integer> prices = Utils.parseTwoNumericValues(priceRange);

        return new PageDto<>(productsPage, () -> {
            // Если диапазон цен был задан, значит происходила выборка по ценам вариантов товаров
            if (prices == null)
                return productsPage.getContent().stream().map(ProductPreviewRespDto::new).toList();
            else
                return productsPage.getContent().stream().map(p -> new ProductPreviewRespDto(p, prices)).toList();
        });
    }

    //Выборка всех категорий в которых задана скидка
    @GetMapping(value = "/get_categories_with_discounts", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<CategoryDtoWithChildren> getCategoriesWithDiscount(@Valid @RequestParam(value = "discount_id") @Min(0) long discountId) {

        Discount discount = Services.discountsService.getById(discountId);

        List<ProductVariant> productVariants = discount.getProductVariants();

        // Множество id категорий
        Set<Long> categoriesIdsSet = new HashSet<>();

        // Добавить и родительские и дочерние категории в один Map
        for (ProductVariant pv : productVariants) {
            Category category = pv.getProduct().getCategory();

            if (categoriesIdsSet.contains(category.getId()))
                continue;

            categoriesIdsSet.add(category.getId());

        }

        List<Category> parentCategories = Services.categoriesService.getAllParentCategories();

        // Сформировать дерево категорий с учётом полученного ранее множества id
        List<CategoryDtoWithChildren> categoriesWithChildren = new ArrayList<>();

        for (Category category : parentCategories) {

            // Dto без подсчёта кол-ва товаров каждой категории
            CategoryDtoWithChildren categoryWithChildren = new CategoryDtoWithChildren(category, null);

            // Рекурсивно выбрать дочерние категории
            categoryWithChildren.setChildCategories(ControllerUtils.findChildCategories(category.getId(), categoriesIdsSet));

            // Если в рекурсивном методе была добавлена хотя бы 1 дочерняя категория
            // В будущем если решу, что нужно выводить всё дерево категорий и помечать те,
            // у которых скидка задана, то просто нужно будет изменить метод findChildCategories
            if (categoryWithChildren.getChildCategories() != null && !categoryWithChildren.getChildCategories().isEmpty())
                categoriesWithChildren.add(categoryWithChildren);

        }


        return categoriesWithChildren;
    }

    // Задать скидку на несколько вариантов
    @PutMapping(value = "/add/to_variants")
    public ResponseEntity<String> addDiscountToVariants(@Valid @RequestBody Map<String, Long[]> productVariantIds,
                                                       @Valid @RequestParam(value = "discount_id") @Min(0) long discountId) {

        // Получение списка id вариантов
        //List<Long> idList = Arrays.stream(productVariantIds.values().stream().toList().get(0)).toList();
        List<Long> idList = Arrays.stream(productVariantIds.get("ids_list")).toList();
        Services.discountsService.addDiscountToPvList(discountId, idList);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(String.format("Скидка с id: %d добавлена к вариантам по списку id", discountId));


    }// addDiscountToVariant

    // Убрать скидку у нескольких варианто
    @PutMapping(value = "/remove/from_variants")
    public ResponseEntity<String> removeDiscountToVariants(@Valid @RequestBody Map<String, Long[]> productVariantIds,
                                                       @Valid @RequestParam(value = "discount_id") @Min(0) long discountId) {

        // Получение списка id вариантов
        //List<Long> idList = Arrays.stream(productVariantIds.values().stream().toList().get(0)).toList();
        List<Long> idList = Arrays.stream(productVariantIds.get("ids_list")).toList();
        Services.discountsService.removeDiscountFromPvList(discountId, idList);

        /*Discount discount = Services.discountsService.getById(discountId);
        ProductVariant changedVariant = Services.productVariantsService.getById(productVariantId);
        boolean removed = changedVariant.getDiscount() == null;*/

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(String.format("Скидка с id: %d удалена у вариантов с заданным списком id", discountId));


    }// removeDiscountToVariants

    // Задать скидку на несколько товаров
    @PutMapping(value = "/add/to_products")
    public ResponseEntity<String> addDiscountToProducts(@Valid @RequestBody Map<String, Long[]> productsIds,
                                                       @Valid @RequestParam(value = "discount_id") @Min(0) long discountId) {

        // Получение списка id товаров
        //List<Long> idList = Arrays.stream(productVariantIds.values().stream().toList().get(0)).toList();
        List<Long> idList = Arrays.stream(productsIds.get("ids_list")).toList();
        Services.discountsService.addDiscountToProductsList(discountId, idList);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(String.format("Скидка с id: %d добавлена к товарам по списку id", discountId));


    }// addDiscountToProducts

    // Убрать скидку у нескольких товаров
    @PutMapping(value = "/remove/from_products")
    public ResponseEntity<String> removeDiscountFromProducts(@Valid @RequestBody Map<String, Long[]> productsIds,
                                                       @Valid @RequestParam(value = "discount_id") @Min(0) long discountId) {

        // Получение списка id вариантов
        //List<Long> idList = Arrays.stream(productsIds.values().stream().toList().get(0)).toList();
        List<Long> idList = Arrays.stream(productsIds.get("ids_list")).toList();
        Services.discountsService.removeDiscountFromProductsList(discountId, idList);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(String.format("Скидка с id: %d удалена у товаров с заданным списком id", discountId));


    }// removeDiscountFromProducts


    // Задать скидку на всю категорию
    @PutMapping(value = "/add/to_category")
    public ResponseEntity<String> addDiscountToCategory(@Valid @RequestParam(value = "category_id") @Min(0) long categoryId,
                                                        @Valid @RequestParam(value = "discount_id") @Min(0) long discountId) {

        Services.discountsService.addDiscountToCategory(discountId, categoryId);
        Discount discount = Services.discountsService.getById(discountId);

        String message = "";

        if (categoryId > 0) {
            Category category = Services.categoriesService.getById(categoryId);
            message = String.format(" '%s' и все её дочерние категории!", category.getName());
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(String.format("Скидка с id: %d добавлена на категорию%s", discount.getId(), message));

    }// addDiscountToCategory

    // Убрать скидку с категории
    @PutMapping(value = "/remove/from_category")
    public ResponseEntity<String> removeDiscountFromCategory(@Valid @RequestParam(value = "category_id") @Min(0) long categoryId,
                                                             @Valid @RequestParam(value = "discount_id") @Min(0) long discountId) {

        Services.discountsService.removeDiscountFromCategories(discountId, List.of(categoryId));
        Discount discount = Services.discountsService.getById(discountId);

        String message = "";

        if (categoryId > 0) {
            Category category = Services.categoriesService.getById(categoryId);
            message = String.format(" '%s' со всех её дочерние категорий!", category.getName());
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(String.format("Скидка с id: %d убрана на категории%s", discount.getId(), message));

    }// removeDiscountFromCategory

}
