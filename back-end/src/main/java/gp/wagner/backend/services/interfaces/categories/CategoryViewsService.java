package gp.wagner.backend.services.interfaces.categories;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.visits.CategoryViews;
import gp.wagner.backend.infrastructure.SimpleTuple;

import java.util.List;


public interface CategoryViewsService {

    //Добавление записи
    void create(CategoryViews categoryViews);
    void create(long visitorId, long categoryId, int count);
    void createOrUpdate(String fingerPrint, long categoryId);

    //Изменение записи
    void update(CategoryViews categoryViews);
    void update(long viewId,long visitorId, long categoryId, int count);

    //Выборка всех записей
    List<CategoryViews> getAll();

    //Выборка записи под id
    CategoryViews getById(Long id);

    //Выборка записи под id
    SimpleTuple<SimpleTuple<Boolean, Category>,List<CategoryViews>> getByCategoryId(long id);

    //Выборка упрощенных
    CategoryViews getSimpleCVByCategoryId(long id);

    //Выборка записи по fingerPrint
    CategoryViews getByVisitorFingerPrint(String fingerPrint);

    //Выборка записи по id посетителя
    CategoryViews getByVisitorAndCategoryId(long id, long categoryId);

}
