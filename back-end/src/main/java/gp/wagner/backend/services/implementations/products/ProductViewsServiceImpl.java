package gp.wagner.backend.services.implementations.products;

import gp.wagner.backend.domain.dto.response.VisitorRespDto;
import gp.wagner.backend.domain.dto.response.product_views.ProductViewRespDto;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.visits.ProductViews;
import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.SortingUtils;
import gp.wagner.backend.infrastructure.enums.AggregateOperationsEnum;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.ServicesUtils;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.enums.ProductsOrVariantsEnum;
import gp.wagner.backend.infrastructure.enums.sorting.VisitorAndViewsSortEnum;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.products.ProductViewsRepository;
import gp.wagner.backend.services.interfaces.products.ProductViewsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductViewsServiceImpl implements ProductViewsService {

    //Репозиторий
    private ProductViewsRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setRepository(ProductViewsRepository repository) {
        this.repository = repository;
    }


    //region Создание
    @Override
    public void create(ProductViews productView) {

        //Проверить наличие записи о посетителе в таблице

        repository.saveAndFlush(productView);
    }

    @Override
    public void create(long visitorId, long productId, int count) {

        if (productId <= 0 || count <= 0 || visitorId <= 0)
            return;

        repository.insertProductView(visitorId, productId, count);

    }

    @Override
    public void createOrUpdate(String fingerPrint, long productId) {
        if (productId <= 0 || fingerPrint.isBlank())
            return;

        //Найти или создать посетителя с заданным отпечатком браузера
        Visitor visitor = Services.visitorsService.saveIfNotExists(fingerPrint);

        //Проверить наличие записи просмотра товара для конкретного посетителя по конкретному товару
        ProductViews productView = getByVisitorAndProductId(visitor.getId(), productId);

        //Если запись не существует, тогда создаём, если запись имеется, тогда увеличить счетчик
        if (productView == null)
            repository.insertProductView(visitor.getId(), productId, 1);
        else{

            // Если прошло < 24ч, тогда просмотр не учитывать
            if (!productView.goneMoreThan(24))
                return;

            productView.setCount(productView.getCount()+1);
            update(productView);
        }

    }
    //endregion

    //region Изменение
    @Override
    public void update(ProductViews productView) {
        if (productView == null)
            return;

        repository.updateProductView(productView.getId(),productView.getVisitor().getId(),
                productView.getProduct().getId(), productView.getCount());
    }

    @Override
    public void update(long viewId, long visitorId, long productId, int count) {
        if (productId <= 0 || count <= 0 || visitorId <= 0)
            return;

        repository.updateProductView(viewId,visitorId, productId, count);
    }
    //endregion

    @Override
    public List<ProductViews> getAll() {
        return repository.findAll();
    }

    @Override
    public ProductViews getById(Long id) {

        if (id == null)
            return null;

        return repository.findById(id).orElseThrow(() -> new ApiException("Не удалось "));
    }

    @Override
    public ProductViews getByVisitorFingerPrint(String fingerPrint) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<ProductViews> query = cb.createQuery(ProductViews.class);

        //Основная таблица для совершения запросов
        Root<ProductViews> root = query.from(ProductViews.class);

        //Присоединить таблицу посетителей
        Join<ProductViews, Visitor> visitorsJoin = root.join("visitor");

        query.where(cb.equal(visitorsJoin.get("fingerprint"), fingerPrint));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public ProductViews getByVisitorAndProductId(long visitorId, long productId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<ProductViews> query = cb.createQuery(ProductViews.class);

        //Основная таблица для совершения запросов
        Root<ProductViews> root = query.from(ProductViews.class);

        //Присоединить таблицу посетителей
        Join<ProductViews, Visitor> visitorsJoin = root.join("visitor");
        Join<ProductViews, Visitor> productsJoin = root.join("product");

        //Выборка по id товара и посетителя
        Predicate predicate = cb.and(
                cb.equal(visitorsJoin.get("id"), visitorId),
                cb.equal(productsJoin.get("id"), productId)
        );

        query.where(predicate);

        List<ProductViews> productViews = entityManager.createQuery(query).getResultList();

        if (productViews.size() != 0)
            return productViews.get(0);
        else
            return null;
    }

    @Override
    public Page<SimpleTuple<Long, Integer>> getAllProductsViews(int pageNum, int limit, Long categoryId, String priceRange, GeneralSortEnum sortEnum) {

        TypedQuery<Tuple> typedQuery = ServicesUtils.getTypedQueryProductsViews(AggregateOperationsEnum.SUM, entityManager, categoryId, priceRange, sortEnum);

        if (pageNum > 0)
            pageNum -= 1;

        typedQuery.setFirstResult(pageNum*limit);
        typedQuery.setMaxResults(limit);

        List<SimpleTuple<Long, Integer>> rawResult = typedQuery.getResultList()
                .stream()
                .map(e -> new SimpleTuple<>(e.get(0, Long.class), e.get(1, Integer.class)))
                .toList();

        // Общее количество записей о просмотрах с такими параметрами
        Long elementsCount = ServicesUtils.getTypedQueryCountProductsViews(entityManager, categoryId, priceRange);

        return new PageImpl<>(rawResult, PageRequest.of(pageNum, limit), elementsCount);
    }

    @Override
    public Page<SimpleTuple<Long, Double>> getAvgProductsViews(int pageNum, int offset, Long categoryId, String priceRange, GeneralSortEnum sortEnum) {

        TypedQuery<Tuple> typedQuery = ServicesUtils.getTypedQueryProductsViews(AggregateOperationsEnum.AVG, entityManager, categoryId, priceRange, sortEnum);

        if (pageNum > 0)
            pageNum -= 1;

        typedQuery.setFirstResult(pageNum*offset);
        typedQuery.setMaxResults(offset);

        List<SimpleTuple<Long, Double>> rawResult = typedQuery.getResultList()
                .stream()
                .map(e -> new SimpleTuple<>(e.get(0, Long.class), e.get(1, Double.class)))
                .toList();

        // Общее количество записей о просмотрах с такими параметрами
        Long elementsCount = ServicesUtils.getTypedQueryCountProductsViews(entityManager, categoryId, priceRange);

        return new PageImpl<>(rawResult, PageRequest.of(pageNum, offset), elementsCount);
    }

    // Сортировка здесь пока что отключена, для улучшения производительности
    @Override
    public Page<SimpleTuple<Long, Integer>> getProductsWithMaxViews(int pageNum, int offset, Long categoryId, String priceRange, float percentage, GeneralSortEnum sortEnum) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Основной запрос выборки товаров с кол-вом просмотров близким к максимальному
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
        Root<ProductViews> root = query.from(ProductViews.class);
        Join<ProductViews, Product> productJoin = root.join("product", JoinType.LEFT);

        // Запрос для подсчёта кол-ва просмотров
        CriteriaQuery<Long> sumsQuery = cb.createQuery(Long.class);
        Root<ProductViews> sumsQueryRoot = sumsQuery.from(ProductViews.class);
        Join<ProductViews, Product> sumsProductJoin = sumsQueryRoot.join("product", JoinType.LEFT);

        // Добавить предикаты фильтрации по категории, цене и производителям
        List<Predicate> predicatesMainQuery = ServicesUtils.collectProductsPredicates(cb, productJoin, query, null, categoryId, priceRange, ProductsOrVariantsEnum.PRODUCTS);
        List<Predicate> predicatesSumsQuery = ServicesUtils.collectProductsPredicates(cb, sumsProductJoin, sumsQuery, null, categoryId, priceRange, ProductsOrVariantsEnum.PRODUCTS);

        // Добавить предикаты для корректного подсчёта сумм просмотров
        if (predicatesSumsQuery != null && !predicatesSumsQuery.isEmpty())
            sumsQuery.where(predicatesSumsQuery.toArray(new Predicate[0]));

        // Рассчитать максимальное кол-во просмотров в найденных товарах
        sumsQuery.select(cb.sum(sumsQueryRoot.get("count")).as(Long.class).alias("sums"))
                .groupBy(sumsProductJoin.get("id"));

        List<Long> sumsList = entityManager.createQuery(sumsQuery).getResultList();
        long maxCount = Collections.max(sumsList);

        maxCount = Math.round(maxCount*(1-percentage));

        // Основной запрос
        Expression<Integer> sumExpression = cb.sum(cb.coalesce(root.get("count"), 0));

        if (predicatesMainQuery != null && !predicatesMainQuery.isEmpty())
            query.where(predicatesMainQuery.toArray(new Predicate[0]));

        query.multiselect(
                productJoin.get("id"),
                sumExpression
        ).groupBy(productJoin.get("id"))
         .having(cb.ge(sumExpression, maxCount));

        //if (sortEnum != null)
        //    query.orderBy(sortEnum == GeneralSortEnum.ASC ? cb.asc(sumExpression) : cb.desc(sumExpression));

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);

        if (pageNum > 0)
            pageNum -= 1;

        typedQuery.setFirstResult(pageNum*offset);
        typedQuery.setMaxResults(offset);

        List<SimpleTuple<Long, Integer>> rawResult = typedQuery.getResultList()
                .stream()
                .map(e -> new SimpleTuple<>(e.get(0, Long.class), e.get(1, Integer.class)))
                .toList();

        // Общее количество записей о просмотрах с такими параметрами
        int elementsCount = ServicesUtils.countMaxProductsViews(entityManager, maxCount, categoryId, priceRange);

        return new PageImpl<>(rawResult, PageRequest.of(pageNum, offset), elementsCount);
    }

    @Override
    public  Page<Tuple>getVisitorsAndProductsViews(int pageNum, int offset, Long categoryId, String priceRange,
                                                    VisitorAndViewsSortEnum sortEnum, GeneralSortEnum sortType) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
        Root<Visitor> root = query.from(Visitor.class);

        Join<Visitor, ProductViews> productViewsJoin = root.join("productViewsList");
        Join<ProductViews, Product> productJoin = productViewsJoin.join("product");

        // Сформировать запрос

        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, productJoin, query, null, categoryId, priceRange,
                ProductsOrVariantsEnum.PRODUCTS);

        if (predicates != null && !predicates.isEmpty())
            query.where(predicates.toArray(new Predicate[0]));

        Expression<Long> countViewsExpression = cb.count(productViewsJoin.get("id"));
        Expression<Integer> viewsSumExpression = cb.sum(productViewsJoin.get("count"));

        SortingUtils.createSortQueryForVisitorsAndViews(cb, query, root, countViewsExpression, viewsSumExpression, sortEnum, sortType);

        query.multiselect(
                root.get("id"),
                countViewsExpression,
                viewsSumExpression
        ).groupBy(root.get("id"));

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);

        if (pageNum > 0)
            pageNum -= 1;

        typedQuery.setFirstResult(pageNum*offset);
        typedQuery.setMaxResults(offset);

        Long elementsCount = ServicesUtils.countVisitorsWithProductsViews(entityManager, categoryId, priceRange);

        return new PageImpl<>(typedQuery.getResultList(), PageRequest.of(pageNum, offset), elementsCount);

    }

}