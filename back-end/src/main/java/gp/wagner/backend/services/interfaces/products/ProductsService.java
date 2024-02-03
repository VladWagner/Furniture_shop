package gp.wagner.backend.services.interfaces.products;


import gp.wagner.backend.domain.dto.request.crud.product.ProductDto;
import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.dto.response.filters.FilterValuesDto;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
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
    Page<Product> getAll(ProductFilterDtoContainer container, Long categoryId, String priceRange, int pageNum, int dataOnPage);

    /**
     * Выборка товаров с фильтрацией и изменённой выборкой по цене. Теперь, если у товара есть вариант с ценой в диапазоне - данный товар будет выбран
     * */
    Page<Product> getAllWithCorrectPrices(ProductFilterDtoContainer container, Long categoryId, String priceRange, int pageNum, int dataOnPage);

    // Метод для подсчёта кол-ва данных по определённому фильтру - для фронта
    long countData(ProductFilterDtoContainer container, Long categoryId, String priceRange);

    // Выборка записи под id
    Product getById(Long id);
    List<Product> getByIdList(List<Long> id);

    //Выборка по категории
    Page<Product> getByCategory(long categoryId,int pageNum, int dataOnPage);

    //Выборка по производителю
    Page<Product> getByProducerPaged(long producerId, int pageNum, int dataOnPage);

    //Подсчет количества товаров по категории
    int countByCategory(long categoryId);

    //Получить диапазон цен у товаров в заданной категории
    FilterValuesDto<Integer> getPricesRangeInCategory(long categoryId);

    //Получить диапазон цен у товаров в нескольких категориях
    FilterValuesDto<Integer> getPricesRangeInCategories(List<Long> categoriesIds);

    // Получить диапазон цен по ключевому слову (при поиске)
    FilterValuesDto<Integer> getPricesRangeByKeyword(String keyword);

    // Удалить по id товара
    boolean deleteById(long id);

    // Восстановить из удаления по id товара. Параметры: id, флаг восстановления наследников
    boolean recoverDeletedById(long id, boolean recoverHeirs);

    // Удалить товары по id производителя
    void deleteByProducerId(Producer producer);

    // Восстановить товары из удаления по id производителя
    void recoverDeletedByProducerId(Producer producer);

    // Скрыть товары по скрытому производителю
    void hideByProducer(Producer producer);

    // Восстановить товары из скрытия по производителю
    void recoverHiddenByProducer(Producer producer);

    // Скрыть товары по скрытой категории
    void hideByCategory(Category category);

    // Восстановить товары из скрытия по категории
    void recoverHiddenByCategory(Category category);

    void hideById(long productId);
    void recoverHiddenById(long productId, boolean recoverHeirs);
}
