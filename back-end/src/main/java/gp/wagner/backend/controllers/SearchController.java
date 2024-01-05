package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.dto.response.product.ProductPreviewRespDto;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.infrastructure.ControllerUtils;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.middleware.Services;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/search")
public class SearchController {

    //Поиск товаров
    @GetMapping(value = "/find_by_keyword", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map.Entry<Long, List<ProductPreviewRespDto>> findProductsByKeyword(
            @RequestParam(value = "key") String key,
            @Valid @RequestPart(value = "filter", required = false) ProductFilterDtoContainer filterContainer,
            @RequestParam(value = "price_range", defaultValue = "") String priceRange,
            @RequestParam(value = "offset") int page,
            @RequestParam(value = "limit") int limit
    ){

        SimpleTuple<Integer, List<Product>> resultSet = Services.searchService.getProductsByKeyword(key.toLowerCase(),
                filterContainer,
                priceRange.isEmpty() ? null : priceRange,
                page, limit);

        if (resultSet.getValue2().size() == 0)
            throw new ApiException(String.format("Товары с заданным ключевым словом '%s' не найдены. Not found!", key.length() > 10 ? key.substring(0,10).trim() : key));

        List<ProductPreviewRespDto> previewRespDtoList = ControllerUtils.getProductsPreviewsList(resultSet.getValue2());

        return  new AbstractMap.SimpleEntry<>(resultSet.getValue1().longValue(), previewRespDtoList);
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
