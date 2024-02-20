package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.dto.response.product.ProductPreviewRespDto;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.ControllerUtils;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.ProductsSortEnum;
import gp.wagner.backend.middleware.Services;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/search")
public class SearchController {

    //Поиск товаров
    @GetMapping(value = "/find_by_keyword", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ProductPreviewRespDto> findProductsByKeyword(
            @RequestParam(value = "key") String key,
            @Valid @RequestPart(value = "filter", required = false) ProductFilterDtoContainer filterContainer,
            @RequestParam(value = "price_range", defaultValue = "") String priceRange,
            @RequestParam(value = "offset") int page,
            @RequestParam(value = "limit") int limit,
            @RequestParam(value = "sort_by", defaultValue = "id")  String sortBy,
            @RequestParam(value = "sort_type", defaultValue = "asc") String sortType
    ){

        Page<Product> resultPage = Services.searchService.getProductsByKeyword(key.toLowerCase(),
                filterContainer,
                priceRange.isEmpty() ? null : priceRange,
                page, limit,
                ProductsSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        if (resultPage.isEmpty())
            throw new ApiException(String.format("Товары с заданным ключевым словом '%s' не найдены. Not found!", key.length() > 10 ? key.substring(0,10).trim() : key));


        SimpleTuple<Integer, Integer> prices = Utils.parseTwoNumericValues(priceRange);

        return new PageDto<>(resultPage, () -> {
            if (prices == null)
                return resultPage.getContent().stream().map(ProductPreviewRespDto::new).toList();
            else
                return resultPage.getContent().stream().map(p -> new ProductPreviewRespDto(p, prices)).toList();
        });
    }

    //Получение предосмотра товаров по вводимому ключевому слову
    @GetMapping(value = "/get_product_preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductPreviewRespDto> getCollectionPreview(@RequestParam(value = "key") String key){

        List<Product> productsPreviewList = Services.searchService.getProductsPreviewByKeyword(key);

        if (productsPreviewList.size() == 0)
            throw new ApiException(String.format("Товары с заданным ключевым словом '%s' не найдены. Not found!", key.length() > 10 ? key.substring(0,10).trim() : key));

        return ControllerUtils.getProductsPreviewsList(productsPreviewList);
    }


}
