package gp.wagner.backend.services.implementations.categories;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.visits.CategoryViews;
import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.PaginationUtils;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.SortingUtils;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.VisitorAndViewsSortEnum;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.categories.CategoryViewsRepository;
import gp.wagner.backend.services.interfaces.categories.CategoryViewsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryViewsServiceImpl implements CategoryViewsService {

    //Репозиторий
    private CategoryViewsRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setRepository(CategoryViewsRepository repository) {
        this.repository = repository;
    }


    //region Создание
    @Override
    public void create(CategoryViews categoryViews) {
        repository.saveAndFlush(categoryViews);

    }

    @Override
    public void create(long visitorId, long categoryId, int count) {

        if (categoryId <= 0 || count <= 0 || visitorId <= 0)
            return;

        repository.insertCategoryView(visitorId, categoryId, count);

    }

    @Override
    public void createOrUpdate(String fingerPrint, String ip, long categoryId) {
        if (categoryId <= 0 || fingerPrint.isBlank())
            return;

        //Найти с заданным отпечатком браузера
        Visitor visitor = Services.visitorsService.saveIfNotExists(fingerPrint, ip);
        visitor.setLastVisit(new Date());
        Services.visitorsService.update(visitor);

        //Найти категорию по заданному id
        Category category = Services.categoriesService.getById(categoryId);

        //Если такой категории не существует, тогда
        if (category == null)
            throw new ApiException(String.format("Category with id: %d doesn't exist!", categoryId));

        CategoryViews categoryViews = getByVisitorAndCategoryId(visitor.getId(), categoryId);

        //Если запись не существует, тогда создаём, если запись имеется, тогда увеличить счетчик
        if (categoryViews == null)
            repository.insertCategoryView(visitor.getId(), categoryId, 1);
        else{

            // Если за эти сутки уже был просмотр - не учитываем его
            if (!categoryViews.goneMoreThan(24))
                return;

            categoryViews.setCount(categoryViews.getCount()+1);
            update(categoryViews);

        }

        /*if (category.getParentCategory() != null)
            //Рекурсивный вызов для увеличения просмотров родительских категорий
            createOrUpdate(fingerPrint, category.getParentCategory().getId());*/
    }

    @Override
    public void createOrUpdateRepeatingCategory(String fingerPrint, String ip, long categoryId) {
        if (categoryId >= 0 || fingerPrint.isBlank())
            return;

        categoryId = Math.abs(categoryId);

        Services.categoriesService.getRepeatingCategoryById(categoryId);

        //Найти с заданным отпечатком браузера
        Visitor visitor = Services.visitorsService.saveIfNotExists(fingerPrint, ip);
        visitor.setLastVisit(new Date());
        Services.visitorsService.update(visitor);

        //Найти категорию по заданному id
        List<Long> categoriesIdsList = Services.categoriesService.getRepeatingCategoryChildren(categoryId);
        List<Category> categories = Services.categoriesService.getByIdList(categoriesIdsList);


        // Найти существующие записи о просмотрах категорий по списку категорий + посетителю
        Map<Long,CategoryViews> categoryViewsMap = repository.findCategoryViewsByCategoryIds(categoriesIdsList)
                .stream()
                .collect(Collectors.toMap(
                        (cv) -> cv.getCategory().getId(),
                        (cv) -> cv,
                        (oldVal, newVal) -> oldVal,
                        HashMap::new
                ));

        // Пройти по всем найденным категориям и создать/изменить записи об их просмотрах
        for (Category category : categories) {

            long existingCategoryId = category.getId();
            CategoryViews cv;

            // Запись в таблице существует
            if (categoryViewsMap.containsKey(existingCategoryId)){
                cv = categoryViewsMap.get(existingCategoryId);

                // Если заданная категория была просмотрена <= суток назад
                if (!cv.goneMoreThan(24))
                    continue;

                cv.setCount(cv.getCount()+1);
                categoryViewsMap.replace(existingCategoryId, cv);
                continue;
            }

            cv = new CategoryViews(null, visitor, category, 1);
            categoryViewsMap.put(existingCategoryId, cv);
        }

        // Сохранить список изменённых/созданных записей
        repository.saveAllAndFlush(categoryViewsMap.values());

    }
    //endregion

    //region Изменение
    @Override
    public void update(CategoryViews categoryViews) {
        if (categoryViews == null)
            return;

        repository.updateCategoryView(categoryViews.getId(),categoryViews.getVisitor().getId(),
                categoryViews.getCategory().getId(), categoryViews.getCount());
    }

    @Override
    public void update(long viewId, long visitorId, long categoryId, int count) {
        if (categoryId <= 0 || count <= 0 || visitorId <= 0)
            return;

        repository.updateCategoryView(viewId,visitorId, categoryId, count);
    }
    //endregion

    @Override
    public List<CategoryViews> getAll() {

        return repository.findAll();
    }


    @Override
    public CategoryViews getById(Long id) {

        if (id == null)
            return null;

        return repository.findById(id).orElse(null);
    }

    //Возвращаем значение вида <<is_parent_category, categoryObj>, views list>
    @Override
    public SimpleTuple<SimpleTuple<Boolean,Category>,List<CategoryViews>> getByCategoryId(long id) {

        //Найти категорию по заданному id
        Category category = Services.categoriesService.getById(id);

        //Если такой категории не существует, тогда
        if (category == null)
            throw new ApiException(String.format("Category with id: %d doesn't exist!", id));

        //Если категория дочерняя, то просто подсчитываем просмотры
        if (category.getParentCategory() != null)
            return new SimpleTuple<>(
                    new SimpleTuple<>(false, category),
                    repository.findCategoryViewsByCategoryId(id))  ;

        //Если категория родительская
        Integer generalViews = repository.countCategoryTreeViews(id);
        CategoryViews categoryViews = new CategoryViews(null, null, category,generalViews != null ? generalViews : 0);

        //Возвращаем список просмотров категорий, состоящий из одного элемента
        return new SimpleTuple<>(
                new SimpleTuple<>(true, category),
                new ArrayList<>(List.of(categoryViews))
        );
    }

    @Override
    public CategoryViews getSimpleCVByCategoryId(long id) {

        //Найти категорию по заданному id
        Category category = Services.categoriesService.getById(id);

        //Если такой категории не существует, тогда
        if (category == null)
            throw new ApiException(String.format("Category with id: %d doesn't exist!", id));

        //Найти все просмотры, включая все дочерние категории
        Integer generalViews = repository.countCategoryTreeViews(id);

        return new CategoryViews(null, null, category,generalViews != null ? generalViews : 0);
    }

    //Получить просмотры категорий по конкретному посетителю
    @Override
    public CategoryViews getByVisitorFingerPrint(String fingerPrint) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<CategoryViews> query = cb.createQuery(CategoryViews.class);

        //Основная таблица для совершения запросов
        Root<CategoryViews> root = query.from(CategoryViews.class);

        //Присоединить таблицу посетителей
        Join<CategoryViews, Visitor> visitorsJoin = root.join("visitor");

        query.where(cb.equal(visitorsJoin.get("fingerprint"), fingerPrint));

        List<CategoryViews> resultList = entityManager.createQuery(query).getResultList();

        if (resultList.size() > 0)
            return resultList.get(0);
        else return null;
    }

    @Override
    public CategoryViews getByVisitorAndCategoryId(long visitorId, long categoryId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<CategoryViews> query = cb.createQuery(CategoryViews.class);

        //Основная таблица для совершения запросов
        Root<CategoryViews> root = query.from(CategoryViews.class);

        //Присоединить таблицу посетителей
        Join<CategoryViews, Visitor> visitorsJoin = root.join("visitor");
        Join<CategoryViews, Visitor> categoriesJoin = root.join("category");

        //Выборка по id категории и посетителя
        Predicate predicate = cb.and(
                cb.equal(visitorsJoin.get("id"), visitorId),
                cb.equal(categoriesJoin.get("id"), categoryId)
        );

        query.where(predicate);

        List<CategoryViews> categoryViews = entityManager.createQuery(query).getResultList();

        if (categoryViews.size() != 0)
            return categoryViews.get(0);
        else
            return null;
    }
    @Override
    public Page<Tuple> getVisitorsAndCategoriesViews(int pageNum, int dataOnPage,
                                                     VisitorAndViewsSortEnum sortEnum, GeneralSortEnum sortType) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
        Root<Visitor> root = query.from(Visitor.class);

        // Присоединения таблиц нужны для сортировки
        Join<Visitor, CategoryViews> categoriesViewsJoin = root.join("categoriesViewsList");

        Expression<Long> countViewsExpression = cb.count(categoriesViewsJoin.get("id"));
        Expression<Integer> viewsSumExpression = cb.sum(categoriesViewsJoin.get("count"));

        SortingUtils.createSortQueryForVisitorsAndViews(cb, query, root, countViewsExpression, viewsSumExpression, sortEnum, sortType);

        query.multiselect(
                root.get("id"),
                countViewsExpression,
                viewsSumExpression
             ).groupBy(root.get("id"));

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);

        if (pageNum > 0)
            pageNum -= 1;

        typedQuery.setFirstResult(pageNum*dataOnPage);
        typedQuery.setMaxResults(dataOnPage);

        Long elementsCount = PaginationUtils.countVisitorsWithProductsViews(entityManager);

        return new PageImpl<>(typedQuery.getResultList(), PageRequest.of(pageNum, dataOnPage), elementsCount);
    }
}