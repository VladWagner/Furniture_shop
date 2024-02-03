package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.infrastructure.SimpleTuple;
import org.springframework.data.domain.Page;

import java.util.List;


public interface SearchService {

    // Выборка товаров по ключевому слову
    Page<Product> getProductsByKeyword(String key, ProductFilterDtoContainer container,
                                       String priceRange, int page, int limit);

    // Выборка небольшого кол-ва товаров по вводимому ключевому слову для предосмотра
    List<Product> getProductsPreviewByKeyword(String key);


}
