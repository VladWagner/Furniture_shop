package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.entites.visits.ProductViews;

import java.util.List;


public interface ProductViewsService {

    //Добавление записи
    void create(ProductViews productView);
    void create(long visitorId, long productId, int count);
    void createOrUpdate(String fingerPrint, long productId);

    //Изменение записи
    void update(ProductViews productView);
    void update(long viewId,long visitorId, long productId, int count);

    //Выборка всех записей
    List<ProductViews> getAll();

    //Выборка записи под id
    ProductViews getById(Long id);

    //Выборка записи по fingerPrint
    ProductViews getByVisitorFingerPrint(String fingerPrint);

    //Выборка записи по id посетителя
    ProductViews getByVisitorAndProductId(long id, long productId);

}
