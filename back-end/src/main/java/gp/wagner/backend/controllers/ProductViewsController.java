package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.dto.response.VisitorRespDto;
import gp.wagner.backend.domain.dto.response.product_views.ProductViewRespDto;
import gp.wagner.backend.domain.dto.response.product_views.VisitorProductViewRespDto;
import gp.wagner.backend.infrastructure.GeneralSortEnum;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.middleware.Services;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/statistic/products_views")
public class ProductViewsController {

    //Выборка всех производителей
    //TODO: попробовать реализовать получение списка id категорий для выборки (на фронте выбирать чек-боксами)
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ProductViewRespDto> getAllProductsViews(@Valid @RequestParam(value = "offset") @Max(100) int pageNum,
                                                           @Valid @RequestParam(value = "limit") @Max(80) int limit,
                                                           @RequestParam(value = "sort", defaultValue = "desc", required = false) String sortType,
                                                           @RequestParam(value = "category_id", defaultValue = "0") Long categoryId,
                                                           @RequestParam(value = "price_range", defaultValue = "") String priceRange) {

        Page<SimpleTuple<Long, Integer>> prodViewsPaged = Services.productViewService.getAllProductsViews(pageNum, limit,
                categoryId < 1 ? null : categoryId, priceRange.isEmpty() ? null : priceRange,
                GeneralSortEnum.getSortType(sortType));

        // Получить найденные id товаров
        List<Long> productsIdList = prodViewsPaged.getContent()
                .stream()
                .map(SimpleTuple::getValue1)
                .toList();

        return new PageDto<>(
                prodViewsPaged, () -> {

            // Найти товары по списку id
            List<ProductViewRespDto> foundProducts = Services.productsService.getByIdList(productsIdList)
                    .stream()
                    .map(ProductViewRespDto::new)
                    .toList();

            Map<Long, ProductViewRespDto> productMap = foundProducts.stream()
                    .collect(Collectors.toMap(ProductViewRespDto::getId, dto -> dto));

            // Для сохранения порядка сортировки создать в худшем случае сложность O(n^2)
            return prodViewsPaged.getContent()
                    .stream()
                    .map(t -> {
                                ProductViewRespDto dto = productMap.get(t.getValue1());

                                if (dto != null)
                                    dto.setViewsCount(t.getValue2());
                                return dto;

                            }
                    )
                    .filter(Objects::nonNull)
                    .toList();
        }
        );
    }

    // Выборка посетителей и просмотренных ими товаров
    @GetMapping(value = "/visitors_views", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<VisitorProductViewRespDto> getAllVisitorsProductsViews(@Valid @RequestParam(value = "offset") @Max(100) int pageNum,
                                                                          @Valid @RequestParam(value = "limit") @Max(80) int limit,
                                                                          @RequestParam(value = "sort", defaultValue = "desc", required = false) String sortType,
                                                                          @RequestParam(value = "category_id", defaultValue = "0") Long categoryId,
                                                                          @RequestParam(value = "price_range", defaultValue = "") String priceRange) {

        Page<SimpleTuple<VisitorRespDto, List<ProductViewRespDto>>> prodViewsPaged = Services.productViewService.getVisitorsAndProductsViews(pageNum, limit,
                categoryId < 1 ? null : categoryId, priceRange.isEmpty() ? null : priceRange,
                GeneralSortEnum.getSortType(sortType));


        return new PageDto<>(prodViewsPaged, () -> prodViewsPaged.getContent().stream()
                .map(t -> new VisitorProductViewRespDto(t.getValue1(), t.getValue2()))
                .toList()
        );
    }//getAllVisitorsProductsViews

    // Среднее кол-во просмотров для каждого товара
    @GetMapping(value = "/avg", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ProductViewRespDto> getAvgProductsViews(@Valid @RequestParam(value = "offset") @Max(100) int pageNum,
                                                           @Valid @RequestParam(value = "limit") @Max(80) int limit,
                                                           @RequestParam(value = "sort", defaultValue = "desc", required = false) String sortType,
                                                           @RequestParam(value = "category_id", defaultValue = "0") Long categoryId,
                                                           @RequestParam(value = "price_range", defaultValue = "") String priceRange) {

        Page<SimpleTuple<Long, Double>> prodViewsPaged = Services.productViewService.getAvgProductsViews(pageNum, limit,
                categoryId < 1 ? null : categoryId, priceRange.isEmpty() ? null : priceRange,
                GeneralSortEnum.getSortType(sortType));

        List<Long> productsIds = prodViewsPaged.getContent()
                .stream()
                .map(SimpleTuple::getValue1)
                .toList();

        return new PageDto<>(prodViewsPaged, () -> {


            // Сформировать ассоциативный массив с парами: productId ↔ Объект ProductViewRespDto для полученного списка
            Map<Long, ProductViewRespDto> dtoMap = Services.productsService.getByIdList(productsIds)
                    .stream()
                    .map(ProductViewRespDto::new)
                    .collect(Collectors.toMap(ProductViewRespDto::getId, dto -> dto));

            return prodViewsPaged.getContent()
                    .stream()
                    .map(
                            t -> {
                                ProductViewRespDto dto = dtoMap.get(t.getValue1());

                                if (dto != null)
                                    dto.setAvgViewsCount(t.getValue2());
                                return dto;
                            }
                    ).toList();
        });
    }//getAvgProductsViews

    // Товары с максимальном кол-вом просмотров
    // Percentage означает топ % просмотров каких товаров выбирать
    @GetMapping(value = "/max", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ProductViewRespDto> getMaxProductsViews(@Valid @RequestParam(value = "offset") @Max(100) int pageNum,
                                                           @Valid @RequestParam(value = "limit") @Max(80) int limit,
                                                           @RequestParam(value = "category_id", defaultValue = "0") Long categoryId,
                                                           @RequestParam(value = "price_range", defaultValue = "") String priceRange,
                                                           @RequestParam(value = "percentage", defaultValue = "0.2") float percentage,
                                                           @RequestParam(value = "sort", defaultValue = "desc", required = false) String sortType) {

        Page<SimpleTuple<Long, Integer>> prodViewsPaged = Services.productViewService.getProductsWithMaxViews(pageNum, limit,
                categoryId < 1 ? null : categoryId,
                priceRange.isEmpty() ? null : priceRange, percentage,
                GeneralSortEnum.getSortType(sortType)
        );

        List<Long> productsIds = prodViewsPaged.getContent()
                .stream()
                .map(SimpleTuple::getValue1)
                .toList();

        return new PageDto<>(prodViewsPaged, () -> {


            // Сформировать ассоциативный массив с парами: productId ↔ Объект ProductViewRespDto для полученного списка
            Map<Long, ProductViewRespDto> dtoMap = Services.productsService.getByIdList(productsIds)
                    .stream()
                    .map(ProductViewRespDto::new)
                    .collect(Collectors.toMap(ProductViewRespDto::getId, dto -> dto));

            return prodViewsPaged.getContent()
                    .stream()
                    .map(
                            t -> {
                                ProductViewRespDto dto = dtoMap.get(t.getValue1());

                                if (dto != null)
                                    dto.setViewsCount(t.getValue2());
                                return dto;
                            }
                    ).toList();
        });
    }//getMaxProductsViews

}
