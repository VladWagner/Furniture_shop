package gp.wagner.backend.services.interfaces.products;

import gp.wagner.backend.domain.dto.response.VisitorRespDto;
import gp.wagner.backend.domain.dto.response.product_views.ProductViewRespDto;
import gp.wagner.backend.domain.entites.visits.ProductViews;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.enums.sorting.VisitorAndViewsSortEnum;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;

import java.util.List;


public interface ProductViewsService {

    // Добавление записи
    void create(ProductViews productView);
    void create(long visitorId, long productId, int count);


    @Async
    void createOrUpdate(String fingerPrint, long productId);

    // Изменение записи
    void update(ProductViews productView);
    void update(long viewId,long visitorId, long productId, int count);

    //Выборка всех записей
    List<ProductViews> getAll();

    // Выборка записи под id
    ProductViews getById(Long id);

    // Выборка записи по fingerPrint
    ProductViews getByVisitorFingerPrint(String fingerPrint);

    // Выборка записи по id посетителя
    ProductViews getByVisitorAndProductId(long id, long productId);

    // Количественная выборка просмотров по товарам
    Page<SimpleTuple<Long, Integer>> getAllProductsViews(int pageNum, int limit, Long categoryId, String priceRange, GeneralSortEnum sortEnum);

    // Выборка среднего кол-ва просмотров на товар
    Page<SimpleTuple<Long, Double>> getAvgProductsViews(int pageNum, int limit, Long categoryId, String priceRange, GeneralSortEnum sortEnum);

    /**
     * Выборка значений значение которых близко к максимальному на 80, 90 и т.д. %
     * То есть вывести 20-30-40% самых просматриваемых товаров
     * @param percentage означает топ % просмотров каких товаров выбирать
     * */
    Page<SimpleTuple<Long, Integer>> getProductsWithMaxViews(int pageNum, int limit, Long categoryId, String priceRange, float percentage, GeneralSortEnum sortEnum);

    // Количественная выборка просмотров по товарам
    Page<Tuple>getVisitorsAndProductsViews(int pageNum, int offset, Long categoryId, String priceRange,
                                            VisitorAndViewsSortEnum sortEnum, GeneralSortEnum sortType);
}
