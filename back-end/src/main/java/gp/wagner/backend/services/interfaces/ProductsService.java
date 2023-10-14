package gp.wagner.backend.services.interfaces;


import gp.wagner.backend.domain.dto.request.crud.product.ProductDto;
import gp.wagner.backend.domain.dto.request.filters.ProductFilterDtoContainer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.infrastructure.SimpleTuple;
import org.springframework.data.domain.Page;

import java.util.List;


public interface ProductsService {

    //Добавление записи
    void create(Product item);

    //Добавление записи
    long create(ProductDto dto);

    //Изменение записи
    void update(Product item);

    //Изменение записи
    void update(ProductDto item);

    //Выборка всех записей

    List<Product> getAll();

    long getMaxId();

    //Выборка всех записей с пагинацией

    Page<Product> getAll(int pageNum, int dataOnPage);

    //Выборка всех записей с фильтрацией
    /*Page<Product>*/SimpleTuple<List<Product>, Integer> getAll(ProductFilterDtoContainer container, Long categoryId, String priceRange, int pageNum, int dataOnPage);


    //Выборка записи под id

    Product getById(Long id);

    //Выборка по категории
    Page<Product> getByCategory(long categoryId,int pageNum, int dataOnPage);

    //Подсчет количества товаров по категории
    int countByCategory(long categoryId);

}
