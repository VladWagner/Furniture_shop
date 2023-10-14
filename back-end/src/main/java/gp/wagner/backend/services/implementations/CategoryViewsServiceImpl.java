package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.visits.CategoryViews;
import gp.wagner.backend.domain.entites.visits.ProductViews;
import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.CategoryViewsRepository;
import gp.wagner.backend.repositories.ProductViewsRepository;
import gp.wagner.backend.services.interfaces.CategoryViewsService;
import gp.wagner.backend.services.interfaces.ProductViewsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    public void createOrUpdate(String fingerPrint, long categoryId) {
        if (categoryId <= 0 || fingerPrint.isBlank())
            return;

        //Найти с заданным отпечатком браузера
        Visitor visitor = Services.visitorsService.getByFingerPrint(fingerPrint);

        //Найти категорию по заданному id
        Category category = Services.categoriesService.getById(categoryId);

        //Если такой категории не существует, тогда
        if (category == null)
            throw new ApiException(String.format("Category with id: %d doesn't exist!", categoryId));

        long createdVisitorId = 0;

        //Если такого посетителя нет, тогда создать его
        if (visitor == null) {

            Visitor v = new Visitor(null, "", fingerPrint);

            /*Services.visitorsService.create("", fingerPrint);
            createdVisitorId = Services.visitorsService.getMaxId();*/

            createdVisitorId = Services.visitorsService.create(v);
        }

        //Проверить наличие записи просмотра категории для конкретного посетителя по конкретной категории
        CategoryViews categoryViews = getByVisitorAndCategoryId(visitor != null ?
                visitor.getId() :
                createdVisitorId, categoryId);

        //Если запись не существует, тогда создаём, если запись имеется, тогда увеличить счетчик
        if (categoryViews == null)
            repository.insertCategoryView(visitor == null ? createdVisitorId : visitor.getId(), categoryId, 1);
        else{
            categoryViews.setCount(categoryViews.getCount()+1);
            update(categoryViews);

        }

        /*if (category.getParentCategory() != null)
            //Рекурсивный вызов для увеличения просмотров родительских категорий
            createOrUpdate(fingerPrint, category.getParentCategory().getId());*/
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

    //Получить просмотры категорий по конкрентому пользователю
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
}