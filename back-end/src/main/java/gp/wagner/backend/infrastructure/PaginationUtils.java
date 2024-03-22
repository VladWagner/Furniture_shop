package gp.wagner.backend.infrastructure;

import gp.wagner.backend.domain.dto.request.admin_panel.CustomerStatRequestDto;
import gp.wagner.backend.domain.dto.request.admin_panel.DatesRangeAndValRequestDto;
import gp.wagner.backend.domain.dto.request.admin_panel.DatesRangeRequestDto;
import gp.wagner.backend.domain.dto.request.crud.CustomerRequestDto;
import gp.wagner.backend.domain.dto.request.filters.CustomersFilterRequestDto;
import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.entites.eav.ProductAttribute;
import gp.wagner.backend.domain.entites.orders.Customer;
import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.entites.orders.OrderAndProductVariant;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.domain.entites.visits.CategoryViews;
import gp.wagner.backend.domain.entites.visits.DailyVisits;
import gp.wagner.backend.domain.entites.visits.ProductViews;
import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.infrastructure.enums.ProductsOrVariantsEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

// Класс для расчётов общего кол-ва элементов при пагинации выборки
public class PaginationUtils {

    // Подсчёт количества посетителей, которые просмотрели определённые категории
    public static Long countVisitorsWithProductsViews(EntityManager entityManager) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<CategoryViews> root = query.from(CategoryViews.class);
        Path<Visitor> visitorPath = root.get("visitor");

        query.select(cb.countDistinct(visitorPath.get("id")));

        return entityManager.createQuery(query).getResultList().get(0);
    }

    // Подсчёт количества товаров для определённого производителя или категории
    public static long countProductsByProducerOrCategory(EntityManager entityManager, long requiredId, Class<?> searchingType) {

        // Проверить, по чём будет происходить выборка
        if (!searchingType.isAssignableFrom(Producer.class) || !searchingType.isAssignableFrom(Producer.class))
            return 0;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Product> root = query.from(Product.class);

        // Определить, по чём будет происходить поиск - производитель или категории
        Path<?> path = searchingType.isAssignableFrom(Producer.class) ? root.get("producer") : root.get("category");

        Predicate predicate = cb.equal(path.get("id"), requiredId);

        query.select(cb.count(root.get("id"))).where(predicate);

        return entityManager.createQuery(query).getResultList().get(0);
    }

    // Подсчёт количества товаров для определённого производителей или категорий
    public static long countProductsByProducersOrCategories(EntityManager entityManager, List<Long> requiredIds, Class<?> searchingType) {

        // Проверить, по чём будет происходить выборка
        if (!searchingType.isAssignableFrom(Producer.class) || !searchingType.isAssignableFrom(Producer.class))
            return 0;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Product> root = query.from(Product.class);

        // Определить, по чём будет происходить поиск - производитель или категории
        Path<?> path = searchingType.isAssignableFrom(Producer.class) ? root.get("producer") : root.get("category");

        Predicate predicate = path.get("id").in(requiredIds);

        query.select(cb.count(root.get("id"))).where(predicate);

        return entityManager.createQuery(query).getResultList().get(0);
    }

    // Подсчёт количества товаров для определённого производителя
    public static long countProductsByProducersOrCategoriesWithPrice(EntityManager entityManager, List<Long> requiredIds, Class<?> searchingType,
                                                                     SimpleTuple<Integer, Integer> pricesRange, ProductsOrVariantsEnum povEnum) {

        // Проверить, по чём будет происходить выборка
        if (!searchingType.isAssignableFrom(Producer.class) || !searchingType.isAssignableFrom(Producer.class))
            return 0;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Product> root = query.from(Product.class);

        // Определить, по чём будет происходить поиск - производитель или категории
        Path<?> path = searchingType.isAssignableFrom(Producer.class) ? root.get("producer") : root.get("category");

        Predicate predicate = cb.and(
                path.get("id").in(requiredIds),
                cb.equal(root.get("isDeleted"), false)/*,
                cb.isNull(root.get("productVariants").get("discount"))*/
        );

        // Сформировать ещё предикат для выборки по цене
        if (pricesRange != null && povEnum != null) {

            // Определить тип выборки по цене (базового варианта или всех вариантов товара)
            Predicate pricePredicate = povEnum == ProductsOrVariantsEnum.PRODUCTS ?
                    ServicesUtils.getProductPricePredicate(pricesRange, root, query, cb) :
                    ServicesUtils.getProductVariantPricePredicate(pricesRange, root, query, cb);

            predicate = cb.and(predicate, pricePredicate);
        }

        query.select(cb.count(root.get("id"))).where(predicate);

        return entityManager.createQuery(query).getResultList().get(0);
    }
    // Подсчёт количества заказов для определённого покупателя по id
    public static long countOrdersByCustomerEmail(EntityManager entityManager, String email, Long id) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        // Составная таблица заказов
        Root<Order> root = query.from(Order.class);

        // Присоединить таблицу покупателей
        Join<Order, Customer> orderCustomerJoin = root.join("customer");

        // Предикат для запроса
        Predicate predicate;

        if (email != null && id != null)
            predicate = cb.and(
                    cb.equal(orderCustomerJoin.get("id"), id),
                    cb.equal(orderCustomerJoin.get("email"), email)
            );
        else if (email != null)
            predicate = cb.equal(orderCustomerJoin.get("email"), email);
        else
            predicate = cb.equal(orderCustomerJoin.get("id"), id);

        query.where(predicate);

        query.select(cb.count(root.get("id"))).where(predicate);

        return entityManager.createQuery(query).getResultList().get(0);
    }

    // Подсчёт общего количества просмотренных товаров для одного покупателя
    public static long countCustomerProductsViews(EntityManager entityManager, CustomerStatRequestDto customerDto){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Основной запрос выборки записей просмотров товаров
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ProductViews> root = query.from(ProductViews.class);
        Path<Visitor> visitorPath = root.get("visitor");
        Path<Customer> customerPath = visitorPath.get("customers");

        Predicate predicate;
        if (customerDto.getId() != null && customerDto.getEmail() != null)
            predicate = cb.or(
                    cb.equal(customerPath.get("id"), customerDto.getId()),
                    cb.equal(customerPath.get("email"), customerDto.getEmail())
            );
        else if(customerDto.getEmail() != null)
            predicate = cb.equal(customerPath.get("email"), customerDto.getEmail());
        else
            predicate = cb.equal(customerPath.get("id"), customerDto.getId());

        query.where(predicate);
        query.select(cb.count(root.get("id")));

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);

        typedQuery.setMaxResults(1);

        return typedQuery.getSingleResult();
    }

    // Подсчёт товаров или их вариантов в заказе
    public static long countProductsOrVariantsOrders(EntityManager entityManager, long searchingId, Class<?> countingType){

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        // Таблица со заказываемыми товарами
        Root<OrderAndProductVariant> root = query.from(OrderAndProductVariant.class);

        // Присоединение сущности productVariant
        Join<OrderAndProductVariant, ProductVariant> pvJoin = root.join("productVariant");
        Path<Product> productPath = null;

        // Если происходит подсчёт количества заказов для товара
        if (countingType == Product.class)
            productPath = pvJoin.get("product");

        Predicate predicate = productPath == null ?
                cb.equal(pvJoin.get("id"), searchingId) :
                cb.equal(productPath.get("id"), searchingId);

        query.where(predicate)
                .select(cb.count(root));

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);

        // Для перестраховки, чтобы избежать падения
        typedQuery.setMaxResults(1);

        return typedQuery.getSingleResult();
    }

    public static long countProductsByFilter(EntityManager entityManager, List<Specification<Product>> specifications, ProductFilterDtoContainer container, Long categoryId, String priceRange){

        //Объект для формирования запросов - построитель запроса
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        //Получить таблицу для запросов
        Root<Product> root = query.from(Product.class);

        // Собираем предикаты категории и дипазона цен отдельно в этом запросе, поскольку для них нужен root именно от текущего query
        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, root, query, container, categoryId, priceRange, ProductsOrVariantsEnum.PRODUCTS);

        // Получить предикат для выборки по заданным фильтрам
        Predicate filterPredicate = Specification.allOf(specifications).toPredicate(root, query, cb);

        //Сформировать запрос
        if (predicates != null && !predicates.isEmpty())
            //Доп.фильтра по категории и ценам, производителям + фильтра по характеристикам
            query.where(cb.and(
                    cb.and(predicates.toArray(new Predicate[0])), filterPredicate));
        else
            query.where(filterPredicate);

        query.select(cb.count(root.get("id")));

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);

        return typedQuery.getResultList().get(0);
    }

    public static long countProductsByFilterPv(EntityManager entityManager, List<Specification<Product>> specifications, ProductFilterDtoContainer container, Long categoryId, String priceRange){

        //Объект для формирования запросов - построитель запроса
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<Product> root = query.from(Product.class);

        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, root, query, container, categoryId, priceRange, ProductsOrVariantsEnum.VARIANTS);

        Predicate filterPredicate = Specification.allOf(specifications).toPredicate(root, query, cb);

        //Сформировать запрос
        if (predicates != null && !predicates.isEmpty())
            //Доп.фильтра по категории и ценам, производителям + фильтра по характеристикам
            query.where(cb.and(
                    cb.and(predicates.toArray(new Predicate[0])), filterPredicate));
        else
            query.where(filterPredicate);

        query.select(cb.countDistinct(root.get("id")));

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList().get(0);
    }

    // Подсчёт количества посетителей, которые просмотрели товары с заданными параметрами
    public static Long countVisitorsWithProductsViews(EntityManager entityManager, Long categoryId, String priceRange){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Visitor> root = query.from(Visitor.class);

        Join<Visitor, ProductViews> productViewsJoin = root.join("productViewsList");
        Join<ProductViews, Product> productJoin = productViewsJoin.join("product");

        // Сформировать запрос

        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, productJoin, query, null, categoryId, priceRange,
                ProductsOrVariantsEnum.PRODUCTS);

        if (predicates != null && !predicates.isEmpty())
            query.where(predicates.toArray(new Predicate[0]));

        query.select(cb.countDistinct(root.get("id")));

        return entityManager.createQuery(query).getResultList().get(0);
    }

    // Подсчёт количества просмотров с максимальными значениями
    public static int countMaxProductsViews(EntityManager entityManager, long maxCount, Long categoryId, String priceRange){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Основной запрос выборки записей просмотров товаров
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ProductViews> root = query.from(ProductViews.class);
        Join<ProductViews, Product> productJoin = root.join("product", JoinType.LEFT);

        // Добавить предикаты
        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, productJoin, query, null, categoryId, priceRange,
                ProductsOrVariantsEnum.PRODUCTS);

        // Основной запрос
        Expression<Integer> sumExpression = cb.sum(cb.coalesce(root.get("count"), 0));

        if (predicates != null && !predicates.isEmpty())
            query.where(predicates.toArray(new Predicate[0]));

        query.select(productJoin.get("id"))
                .groupBy(productJoin.get("id"))
                .having(cb.ge(sumExpression, maxCount));


        return entityManager.createQuery(query).getResultList().size();
    }

    // Подсчёт количества товаров по заданному ключевому слову
    public static long countProductsByKeyword(String key, EntityManager entityManager, List<Specification<Product>> specifications, ProductFilterDtoContainer container, String priceRange){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<Product> root = query.from(Product.class);

        Join<Product, ProductVariant> productVariantJoin = root.join("productVariants");

        //Список предикатов для поиска
        List<Predicate> searchPredicates = new ArrayList<>();

        searchPredicates.add(cb.like(root.get("name"),key));
        searchPredicates.add(cb.like(root.get("description"),key));
        searchPredicates.add(cb.like(productVariantJoin.get("title"),key));
        searchPredicates.add(cb.like(root.get("producer").get("producerName"), key));

        // Сформировать предикаты фильтрации по цене и производителям
        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, root, query, container, null, priceRange,
                ProductsOrVariantsEnum.VARIANTS);

        // Спецификации для фильтрации по характеристикам
        Predicate featuresPredicate = Specification.allOf(specifications).toPredicate(root, query, cb);

        //Сформировать запрос
        if (predicates != null && !predicates.isEmpty())
            //Доп.фильтра по категории и ценам + фильтра по характеристикам
            query.where(cb.and(
                            cb.and(cb.or(searchPredicates.toArray(new Predicate[0]))
                                    ,featuresPredicate)),
                    cb.and(predicates.toArray(new Predicate[0]))
            ).distinct(true);
        else
            query.where(cb.and( cb.or(searchPredicates.toArray(new Predicate[0])), featuresPredicate)).distinct(true);

        query.select(cb.countDistinct(root.get("id")));

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);

        return typedQuery.getResultList().get(0);

    }

    // Подсчёт общего кол-ва пользователей по ключевому слову в имени или логине
    public static long countUsersByKeyword(String key, EntityManager entityManager, Specification<User> specification){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<User> root = query.from(User.class);

        List<Predicate> predicates = new ArrayList<>();

        // Условие выборки по ключевым словам
        predicates.add(cb.or(
                cb.like(root.get("name"), key),
                cb.like(root.get("userLogin"), key)
        ));

        // Снова создать предикат из спецификации, но уже для другого CriteriaQuery
        if (specification != null)
            predicates.add(specification.toPredicate(root, query, cb));

        query.where(predicates.toArray(new Predicate[0]));

        query.select(cb.countDistinct(root.get("id")));

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);

        return typedQuery.getResultList().get(0);

    }

    // Подсчёт количества записей о посещении магазина за определённый период
    public static long countDailyVisitsInPeriod(EntityManager entityManager, DatesRangeRequestDto datesRangeDto){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<DailyVisits> root = query.from(DailyVisits.class);

        Predicate selectionPredicate = cb.between(root.get("date"), datesRangeDto.getMin(), datesRangeDto.getMax());

        query.where(selectionPredicate);

        query.select(cb.count(root.get("id")));

        return entityManager.createQuery(query).getResultList().get(0);
    }

    // Подсчёт количества записей о посещении магазина за определённый период с максимальным количеством самих посещений
    public static long countTopDailyVisitsInPeriod(EntityManager entityManager, DatesRangeRequestDto datesRangeDto, int maxViewsCount){

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<DailyVisits> root = query.from(DailyVisits.class);

        // Сформировать предикат для выборки записей посещений за определеённый период и кол-во самих посещений близко к максимальному
        Predicate selectionPredicate = cb.between(root.get("date"), datesRangeDto.getMin(), datesRangeDto.getMax());
        selectionPredicate = cb.and(
                selectionPredicate,
                cb.ge(root.get("countVisits"), maxViewsCount)
        );

        query.where(selectionPredicate)
                .select(cb.count(root.get("id")));

        return entityManager.createQuery(query).getResultList().get(0);
    }

    // Подсчёт количества записей о посещении магазина за определённый период с максимальным количеством самих посещений
    public static long countProductAttributesByCategory(EntityManager entityManager, long categoryId){

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ProductAttribute> root = query.from(ProductAttribute.class);

        Predicate predicate = cb.equal(root.get("categories").get("id"), categoryId);

        query.select(cb.count(root.get("id"))).where(predicate);

        return entityManager.createQuery(query).getResultList().get(0);
    }

    // Подсчёт количества покупателей с заданным фильтром
    public static long countCustomers(EntityManager entityManager, CustomersFilterRequestDto filterDto){

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Customer> root = query.from(Customer.class);
        Join<Customer, User> userJoin = root.join("user", JoinType.LEFT);

        Expression<Boolean> isRegisteredExp = cb.isNotNull(userJoin);

        //region Подзапрос 1 - общее кол-во заказов для каждого покупателя
        Subquery<Long> ordersCountSubquery = query.subquery(Long.class);
        Root<Order> ordersCountRoot = ordersCountSubquery.from(Order.class);
        ordersCountSubquery.where(cb.equal(ordersCountRoot.get("customer").get("id"), root.get("id")));
        //endregion

        Expression<Long> ordersCountExp = ordersCountSubquery.select(cb.count(ordersCountRoot.get("id")));

        //region Подзапрос 2 - кол-во заказанных товаров во всех orders покупателя
        Subquery<Integer> ordersUnitsCountSubquery = query.subquery(Integer.class);
        Root<Order> ordersUnitsCountRoot = ordersUnitsCountSubquery.from(Order.class);
        ordersUnitsCountSubquery.where(cb.equal(ordersUnitsCountRoot.get("customer").get("id"), root.get("id")));
        //endregion

        Expression<Integer> ordersUnitsCountExp = ordersUnitsCountSubquery.select(
                cb.sum(ordersUnitsCountRoot.get("generalProductsAmount"))
        );

        //region Подзапрос 3 - средняя цена товара в заказах каждого покупателя
        Subquery<Double> avgOrderedUnitPriceSubquery = query.subquery(Double.class);
        Root<Order> avgOrderedUnitPriceRoot = avgOrderedUnitPriceSubquery.from(Order.class);
        avgOrderedUnitPriceSubquery.where(cb.equal(avgOrderedUnitPriceRoot.get("customer").get("id"), root.get("id")));
        //endregion

        Expression<Double> avgUnitPriceExp = avgOrderedUnitPriceSubquery.select(
                cb.avg(cb.quot(avgOrderedUnitPriceRoot.get("sum"), avgOrderedUnitPriceRoot.get("generalProductsAmount")))
        );

        //region Подзапрос 4 - средняя цена товара в заказах каждого покупателя
        Subquery<Integer> ordersSumsSubquery = query.subquery(Integer.class);
        Root<Order> ordedrsSumsRoot = ordersSumsSubquery.from(Order.class);
        ordersSumsSubquery.where(cb.equal(ordedrsSumsRoot.get("customer").get("id"), root.get("id")));


        //endregion

        Expression<Integer> ordersSumsExp = ordersSumsSubquery.select(
                cb.sum(ordedrsSumsRoot.get("sum"))
        );


        // Сформировать предикаты по спецификации из фильтра
        Predicate predicate = filterDto.getId() == null ? cb.and(ServicesUtils.collectCustomersPredicates(cb, root, filterDto, isRegisteredExp,
                ordersCountExp, ordersUnitsCountExp, ordersSumsExp, avgUnitPriceExp).toArray(new Predicate[0])) :
                cb.equal(root.get("id"), filterDto.getId());

        query.select(cb.count(root.get("id"))).where(predicate);

        return entityManager.createQuery(query).getResultList().get(0);
    }
}
