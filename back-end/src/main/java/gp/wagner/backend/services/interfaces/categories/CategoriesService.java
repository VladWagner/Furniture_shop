package gp.wagner.backend.services.interfaces.categories;

import gp.wagner.backend.domain.entites.categories.Category;

import java.util.List;


public interface CategoriesService {

    //Добавление записи
    void create(Category category);

    //Добавление записи с проверкой на наличие повторяющейся категории
    long createAndCheckRepeating(String categoryName, Integer parentCategoryId);

    //Изменение записи
    void update(Category category);

    //Выборка всех записей
    List<Category> getAll();

    //Выборка записи под id
    Category getById(Long id);

    //Выборка id дочерних категорий в родительской на всех уровнях рекурсии
    List<Long> getAllChildCategories(int id);

    //Выборка id дочерних категорий в родительской на одном уровне рекурсии
    List<Long> getChildCategories(int id);

}
