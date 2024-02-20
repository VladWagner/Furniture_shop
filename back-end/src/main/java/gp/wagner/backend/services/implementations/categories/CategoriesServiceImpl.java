package gp.wagner.backend.services.implementations.categories;

import gp.wagner.backend.domain.dto.request.crud.CategoryRequestDto;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.categories.RepeatingCategory;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.domain.exceptions.suppliers.ParentlessCategoryAlreadyExists;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.categories.CategoriesRepository;
import gp.wagner.backend.repositories.categories.SubCategoriesRepository;
import gp.wagner.backend.services.interfaces.categories.CategoriesService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriesServiceImpl implements CategoriesService {

    @PersistenceContext
    private EntityManager entityManager;

    //Репозиторий
    private CategoriesRepository categoriesRepository;


    private SubCategoriesRepository subCategoriesRepository;

    @Autowired
    public void setCategoriesRepository(CategoriesRepository categoriesRepository) {
        this.categoriesRepository = categoriesRepository;
    }

    @Autowired
    public void setSubCategoriesRepository(SubCategoriesRepository subCategoriesRepository) {
        this.subCategoriesRepository = subCategoriesRepository;
    }

    @Override
    //Добавление записи
    public void create(Category category) {
        if (category != null)
            categoriesRepository.saveAndFlush(category);
    }


    @Override
    public long createAndCheckRepeating(String categoryName, Long parentCategoryId) {

        // Найти существующую, повторяющуюся категорию по имени
        Optional<RepeatingCategory> repeatingCategory = subCategoriesRepository.findRepeatingCategoryByName(categoryName);

        // Попытка найти родительскую категорию
        Category parentCategory = parentCategoryId != null ? categoriesRepository.findById(parentCategoryId).orElse(null) : null;

        // Имеется ли заданная родительская категория в БД, если нет, то заданная категория будет родительской
        if (parentCategory == null)
           return handleParentlessCategory(categoryName, repeatingCategory).getId();

        // Обработка создаваемой категории с заданным родителем
        return handleCategoryWithParent(categoryName, parentCategory, repeatingCategory).getId();

    }

    // Изменение категории с проверками на повторения
    @Override
    public Category updateAndCheckRepeating(CategoryRequestDto dto) {

        if (dto == null || dto.getId() == null)
            throw new ApiException("Не получилось изменить объект Category. Задан некорректный DTO!");

        Category oldCategory = categoriesRepository.findById(dto.getId())
                .orElseThrow(() -> new ApiException(String.format("Категория с Id: %d не найдена!",dto.getId())));

        Long oldCategoryParentId = oldCategory.getParentCategory() != null ? oldCategory.getParentCategory().getId() : null;
        // Был ли изменён родитель категории
        boolean parentChanged = (oldCategoryParentId != null && dto.getParentId() != null
                && !oldCategoryParentId.equals(dto.getParentId())) ||
                (oldCategoryParentId != null && dto.getParentId() == null) ||
                (oldCategoryParentId == null && dto.getParentId() != null);

        // Если задано новое имя категорий или изменён родитель для того же имени
        if (dto.getCategoryName() != null && !oldCategory.getName().equals(dto.getCategoryName()) || parentChanged)
            changeNameOfUpdatingCategory(dto, oldCategory);

        boolean oldShowValue = oldCategory.getIsShown();

        oldCategory.setIsShown(dto.getIsShown()!= null ? dto.getIsShown() : oldShowValue);

        categoriesRepository.saveAndFlush(oldCategory);

        // Если категория была скрыта
        if (!oldCategory.getIsShown() && oldShowValue)
            Services.productsService.hideByCategory(oldCategory);
        // Если категорию восстановили из скрытия и задан флаг восстановления всех связанных сущностей
        else if (dto.getIsDisclosed() && dto.getDiscloseHeirs())
            Services.productsService.recoverHiddenByCategory(oldCategory);

        return oldCategory;

    }

    // Вспомогательный метод обработки создания категории без родителя
    private Category handleParentlessCategory(String categoryName, Optional<RepeatingCategory> repeatingCategory){

        // Проверить, существует ли повторяющаяся категория
        if (repeatingCategory.isPresent()) {

            // Найти категорию, которая использует повторяющуюся категория и не имеет родителя
            Category existingCategory = categoriesRepository.findCategoryByRepeatingCategoryAndParent(repeatingCategory.get(),
                    null).orElse(null);

            // Если найдена категория использующая ту же повторяющуюся категорию, тогда это родительская категория, которая уже существует
            if (existingCategory != null)
                throw new ParentlessCategoryAlreadyExists(existingCategory.getId(), categoryName).get();

        } else {

            // Проверить наличие категории с такими же именем (неважно является ли parent_id null)
            Category existingCategory = categoriesRepository.findCategoryByName(categoryName).orElse(null);

            // Если уже существует категория с таким же именем, что и создаваемая, но при этом у неё есть родитель
            if (existingCategory != null && existingCategory.getParentCategory() != null) {
                RepeatingCategory newRepeatingCategory = repeatingCategory
                        .orElseGet(() -> subCategoriesRepository.saveAndFlush(new RepeatingCategory(null, categoryName)));

                existingCategory.setRepeatingCategory(newRepeatingCategory);
                existingCategory.setName(null);

                categoriesRepository.saveAndFlush(existingCategory);

                // Создать новую категорию с повторяющимся именем
                return categoriesRepository.saveAndFlush(
                        new Category(null, null, newRepeatingCategory, null)
                );
            }
            // Если есть категория с тем же именем, что и создаваемая, но без родителя. Тогда это так же самая категория и мы её не создаём
            else if (existingCategory != null)
                throw new ParentlessCategoryAlreadyExists(existingCategory.getId(), categoryName).get();

        }//

        return categoriesRepository.saveAndFlush(new Category(null, repeatingCategory.isEmpty() ? categoryName : null,
                repeatingCategory.orElse(null), null));
    }

    // Вспомогательный метод обработки создания категории с родителем
    private Category handleCategoryWithParent(String categoryName, Category parentCategory, Optional<RepeatingCategory> repeatingCategory){

        if (parentCategory == null)
            throw new ApiException(String.format("Родительская категория для создаваемой категории '%s'не может быть == null!", categoryName));

        // Проверить наличие категории с такими же именем, либо использующую туже повторяющуюся категорию с заданным родителем
        Category existingCategory = categoriesRepository.findCategoryByName(categoryName)
                .orElse(repeatingCategory.isPresent() ?
                        categoriesRepository.findCategoryByRepeatingCategoryAndParent(repeatingCategory.get(), parentCategory).orElse(null) :
                        null);

        long parentCategoryId = parentCategory.getId();

        // Если категория повторяется по имени, но они принадлежат разным родительским категориям и при этом заданная родительская категория != существующей категории
        boolean parentsAreNotEqual = existingCategory != null &&
                (existingCategory.getParentCategory() == null || !existingCategory.getParentCategory().getId().equals(parentCategoryId));
        if (existingCategory != null && !existingCategory.getId().equals(parentCategoryId) && parentsAreNotEqual) {

            // Создать новую или использовать существующую категорию
            RepeatingCategory newRepeatingCategory = repeatingCategory
                    .orElseGet(() -> subCategoriesRepository.saveAndFlush(new RepeatingCategory(null, categoryName)));

            // Изменить существующую категорию, имя которой совпадает с создаваемой, но родитель отличается
            // Данной категории в любом случае нужно заменить поле name, поскольку оно совпадает с создаваемой категорией (даже если повторяющаяся категория найдена)
            existingCategory.setRepeatingCategory(newRepeatingCategory);
            existingCategory.setName(null);

            // Сохранить изменённую категорию
            categoriesRepository.saveAndFlush(existingCategory);

            // Создать новую категорию с повторяющимся именем
            return categoriesRepository.saveAndFlush(
                    new Category(null, null, newRepeatingCategory, parentCategory)
            );

        }
        // Если нашли существующую категорию с таким же названием и она принадлежат к той же родительской категории, что и добавляемая
        else if (existingCategory != null)
            throw new ApiException(String.format("""
                    При добавлении категории '%s' с id родительской категорией %d возникла ошибка.
                    Уже существует категория '%1$s' и её id: %d
                    """, categoryName, parentCategoryId, existingCategory.getId()));

        // Если есть родительская категория и повторяющихся категорий в основной таблице нет
        return categoriesRepository.saveAndFlush(new Category(null, repeatingCategory.isEmpty() ? categoryName : null,
                repeatingCategory.orElse(null), parentCategory)
        );
    }

    private void changeNameOfUpdatingCategory(CategoryRequestDto dto, Category prevCategory){

        Category parentCategory = dto.getParentId() != null ?
                categoriesRepository.findById(dto.getParentId()).orElse(null) : null;

        // Найти существующую, повторяющуюся категорию по имени
        Optional<RepeatingCategory> repeatingCategory = subCategoriesRepository.findRepeatingCategoryByName(dto.getCategoryName());

        // Флаг смены названия категории
        boolean nameChanged = false;

        // Если родительская категория не задана/была убрана
        if (parentCategory == null && repeatingCategory.isPresent())
        {
            // Найти категорию, которая использует повторяющуюся категория и не имеет родителя
            Category existingCategory = categoriesRepository.findCategoryByRepeatingCategoryAndParent(repeatingCategory.get(),
                    null).orElse(null);

            // Если найдена категория использующая ту же повторяющуюся категорию, тогда это родительская категория, которая уже существует
            if (existingCategory != null)
                throw new ParentlessCategoryAlreadyExists(existingCategory.getId(), dto.getCategoryName()).get();
        }
        else if (parentCategory == null) {

            // Найти существующую категорию с задаваемым именем
            Category existingCategory = categoriesRepository.findCategoryByName(dto.getCategoryName()).orElse(null);

            // Если существует категория с таким же именем и при этом у неё задан родитель
            if (existingCategory != null && existingCategory.getParentCategory() != null) {
                prevCategory.setName(null);
                prevCategory.setRepeatingCategory(createAndSetRepeatingCategory(existingCategory));
                prevCategory.setParentCategory(null);
                nameChanged = true;
            }
            else if (existingCategory != null)
                throw new ParentlessCategoryAlreadyExists(existingCategory.getId(), dto.getCategoryName()).get();
        }

        // Если родительская категория задана
        if (parentCategory != null){

            long parentCategoryId = parentCategory.getId();

            // Найти существующую категорию по названию/по повторяющейся категории
            Category existingCategory = categoriesRepository.findCategoryByName(dto.getCategoryName())
                    .orElse(repeatingCategory.flatMap(category -> categoriesRepository.findCategoryByRepeatingCategoryAndParent(category,
                            parentCategory)).orElse(null));

            // Проверить не совпадают ли родительские категории
            boolean parentsAreNotEqual = existingCategory != null &&
                    (existingCategory.getParentCategory() == null || !existingCategory.getParentCategory().getId().equals(parentCategoryId));

            // Если категория существует и она не является родительской и родитель существующей категории != редактируемой
            if (existingCategory != null && !existingCategory.getId().equals(parentCategoryId) && parentsAreNotEqual) {

                prevCategory.setName(null);
                prevCategory.setRepeatingCategory(createAndSetRepeatingCategory(existingCategory));
                prevCategory.setParentCategory(parentCategory);
                nameChanged = true;
            }
            // Если нашли существующую категорию с таким же названием и она принадлежат к той же родительской категории, что и добавляемая
            else if (existingCategory != null)
                throw new ApiException(String.format("""
                    При изменении категории '%s' с id родительской категорией %d возникла ошибка.
                    Уже существует категория '%1$s' и её id: %d
                    """, dto.getCategoryName(), parentCategoryId, existingCategory.getId()));
        }

        // Если никакие из вышеописанных условий не сработали, тогда просто установить имя в categoryName
        if (!nameChanged){
            prevCategory.setName(repeatingCategory.isEmpty() ? dto.getCategoryName() : null);
            prevCategory.setRepeatingCategory(repeatingCategory.orElse(null));
            prevCategory.setParentCategory(parentCategory);
        }

    }

    // Создать повторяющуюся категорию
    private RepeatingCategory createAndSetRepeatingCategory(Category category){

        if (category.getName() == null && category.getRepeatingCategory() != null)
            return category.getRepeatingCategory();

       RepeatingCategory newRepeatingCategory = subCategoriesRepository.saveAndFlush(new RepeatingCategory(null, category.getName()));

       category.setRepeatingCategory(newRepeatingCategory);
       category.setName(null);

       categoriesRepository.saveAndFlush(category);

       return newRepeatingCategory;
    }

    @Override
    //Изменение записи
    public void update(Category category) {
        if (category != null)
            categoriesRepository.saveAndFlush(category);
    }

    public void delete(Category category) {
        if (category != null)
            categoriesRepository.delete(category);
    }

    public void deleteById(Long id) {
        if (id != null)
            categoriesRepository.deleteById(id);
    }

    @Override
    //Выборка всех категорий в БД
    public List<Category> getAll() {
        return categoriesRepository.findAll();
    }

    // Получить все дочерние категории - у которых внешний ключ на родительскую категорию != null
    @Override
    public List<Category> getAllNotParentCategories() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Category> query = cb.createQuery(Category.class);
        Root<Category> root = query.from(Category.class);

        Predicate predicate = cb.isNotNull(root.get("parentCategory"));

        query.where(predicate);

        return entityManager.createQuery(query).getResultList();
    }

    // Получить все родительские (корневые) категории
    @Override
    public List<Category> getAllParentCategories() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Category> query = cb.createQuery(Category.class);
        Root<Category> root = query.from(Category.class);

        Predicate predicate = cb.isNull(root.get("parentCategory"));

        query.where(predicate);

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    //Выборка записи по id
    public Category getById(Long id) {
        if (id == null)
            throw new ApiException("Id категории для поиска задано некорректно!");

        return categoriesRepository.findById(id).orElseThrow(() -> new ApiException(String.format("Не удалось найти категорию с id: %d!", id)));
    }

    @Override
    public RepeatingCategory getRepeatingCategoryById(Long id) {
        if (id == null)
            throw new ApiException("Id категории для поиска задано некорректно!");

        return categoriesRepository.findRepeatingCategoryById(id)
                .orElseThrow(() -> new ApiException(String.format("Не удалось найти повторяющуюся категорию с id: %d!", id)));
    }

    // Получить дочерние категории на всю глубину дерева
    @Override
    public List<Long> getAllChildCategories(long id) {

        if (id <= 0)
            throw new ApiException(String.format("Id %d is incorrect!", id));

        return categoriesRepository.getAllChildCategoriesIds(id);
    }

    @Override
    public List<Long> getAllChildCategories(List<Long> idsList) {

        if (idsList == null || idsList.isEmpty())
            throw new ApiException("Список id категорий задан неверно!");

        return categoriesRepository.getAllChildCategoriesIdsByIdsList(idsList);
    }

    // Получить дочерние категории на одном уровне дерева
    @Override
    public List<Long> getChildCategories(long id) {

        if (id <= 0)
            throw new ApiException(String.format("Id %d is incorrect!", id));

        return categoriesRepository.getChildCategoriesIds(id).orElse(null);
    }

    @Override
    public void hideById(long categoryId) {
        Category category = getById(categoryId);

        if (!category.getIsShown())
            throw new ApiException(String.format("Категория с id: %d уже скрыта!", category.getId()));

        category.setIsShown(false);

        Services.productsService.hideByCategory(category);

        categoriesRepository.saveAndFlush(category);
    }

    @Override
    public void recoverHiddenById(long categoryId, boolean recoverHeirs) {

        Category category = getById(categoryId);

        if (category.getIsShown())
            throw new ApiException(String.format("Категория с id: %d не была скрыта!", category.getId()));

        category.setIsShown(true);

        Services.productsService.recoverHiddenByCategory(category);

        categoriesRepository.saveAndFlush(category);
    }

    @Override
    public List<Category> getByIdList(List<Long> categoriesIds) {

        if (categoriesIds == null)
            throw new ApiException("Найти категории по списку id не удалось. Список id задан некорректно!");

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Category> query = cb.createQuery(Category.class);
        Root<Category> root = query.from(Category.class);

        Predicate predicate = root.get("id").in(categoriesIds);

        query.where(predicate);

        return entityManager.createQuery(query).getResultList();
    }

    // Найти категории использующие повторяющуюся категорию
    @Override
    public List<Long> getRepeatingCategoryChildren(Long repeatingCategoryId) {

        return categoriesRepository.getCategoriesIdsByRepeatingCategory(repeatingCategoryId)
                .orElseThrow(
                        () -> new ApiException(String.format("Для повторяющейся категории с id: %d не удалось найти использующие её основные категории!",
                                repeatingCategoryId))
                );
    }

    @Override
    public List<Long> getRepeatingCategoryChildrenWithHeirs(Long repeatingCategoryId) {

        List<Long> categoriesIds = categoriesRepository.getCategoriesIdsByRepeatingCategory(repeatingCategoryId)
                .orElseThrow(
                        () -> new ApiException(String.format("Для повторяющейся категории с id: %d не удалось найти использующие её основные категории!",
                                repeatingCategoryId))
                );

        return getAllChildCategories(categoriesIds);
    }

    // Найти категории использующие повторяющееся категории по списку id. Так же происходит поиск дочерних категорий у найденных категорий
    @Override
    public List<Long> getRepeatingCategoriesChildrenWithHeirs(List<Long> repeatingCategoryIdsList) {

        List<Long> foundCategoriesIds = categoriesRepository.getCategoriesIdsByRepeatingCategoriesIds(repeatingCategoryIdsList)
                .orElseThrow(
                        () -> new ApiException("Для повторяющейся категории с заданным списком не удалось найти использующие их основные категории!")
                );

        return getAllChildCategories(foundCategoriesIds);
    }

}
