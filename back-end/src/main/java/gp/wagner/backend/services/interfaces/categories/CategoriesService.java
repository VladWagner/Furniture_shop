package gp.wagner.backend.services.interfaces.categories;

import gp.wagner.backend.domain.dto.request.crud.CategoryRequestDto;
import gp.wagner.backend.domain.entities.categories.Category;
import gp.wagner.backend.domain.entities.categories.RepeatingCategory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface CategoriesService {

    //Добавление записи
    void create(Category category);

    //Изменение записи
    Category updateAndCheckRepeating(CategoryRequestDto dto, MultipartFile file)  throws Exception;

    //Добавление записи с проверкой на наличие повторяющейся категории
    long createAndCheckRepeating(String categoryName, Long parentCategoryId, MultipartFile file) throws Exception;

    //Изменение записи
    void update(Category category);

    //Выборка всех записей
    List<Category> getAll();

    //Выборка не родительских категорий
    List<Category> getAllNotParentCategories();

    List<Category> getAllParentCategories();

    //Выборка записи под id
    Category getById(Long id);
    RepeatingCategory getRepeatingCategoryById(Long id);

    //Выборка id дочерних категорий в родительской на всех уровнях рекурсии
    List<Long> getAllChildCategories(long id);
    List<Long> getAllChildCategories(List<Long> id);

    //Выборка id дочерних категорий в родительской на одном уровне рекурсии
    List<Long> getChildCategoriesIds(long id);
    List<Category> getChildCategories(long id);

    // Скрыть категорию по id + скрыть все её товары
    void hideById(long categoryId);

    // Восстановить из скрытия по id категории + восстановить все его товары и варианты
    void recoverHiddenById(long categoryId, boolean recoverHeirs);

    // Получить категории по списку id
    List<Category> getByIdList(List<Long> categoriesIds);

    List<Long> getRepeatingCategoryChildren(Long repeatingCategoryId);
    List<Long> getRepeatingCategoryChildrenWithHeirs(Long repeatingCategoryId);

    List<Long> getRepeatingCategoriesChildrenWithHeirs(List<Long> repeatingCategoryIdsList);
}
