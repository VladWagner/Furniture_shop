package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.products.Producer;

import java.util.List;


public interface ProducersService {

    //Добавление записи
    void create(Producer producer);

    //Изменение записи
    void update(Producer producer);

    void deleteById(long id);

    //Выборка всех записей
    List<Producer> getAll();

    //Выборка записи под id
    Producer getById(long id);

    //Выборка производителей в определённой категории
    List<Producer> getProducersByCategory(long categoryId);

    //Выборка производителей в нескольких категориях
    List<Producer> getProducersInCategories(List<Long> categoriesIds);

    List<Producer> getProducersByProductKeyword(String keyword);
}
