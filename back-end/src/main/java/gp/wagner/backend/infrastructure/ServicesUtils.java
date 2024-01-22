package gp.wagner.backend.infrastructure;

import gp.wagner.backend.domain.dto.request.admin_panel.OrdersAndBasketsCountFiltersRequestDto;
import gp.wagner.backend.domain.dto.request.crud.CustomerRequestDto;
import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.entites.baskets.Basket;
import gp.wagner.backend.domain.entites.baskets.BasketAndProductVariant;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.orders.Customer;
import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.entites.orders.OrderAndProductVariant;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.domain.entites.visits.ProductViews;
import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.infrastructure.enums.AggregateOperationsEnum;
import gp.wagner.backend.infrastructure.enums.GeneralSortEnum;
import gp.wagner.backend.middleware.Services;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;

//Класс для вынесения повторяющихся и вспомогательных методов из сервисов
public class ServicesUtils<T> {

    //Создание предиката для фильтрации товаров по цене
    public static Predicate getPricePredicate(String priceRange, From<?, ?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        //Присоединить таблицу вариантов товаров
        Join<Product, ProductVariant> productProductVariantJoin = root.join("productVariants");

        //Создание подзапроса для получения мин.id варианта товара - базового варианта товара
        Subquery<Long> subqueryId = query.subquery(Long.class);
        Root<ProductVariant> subQueryRoot = subqueryId.from(ProductVariant.class);
        subqueryId.select(cb.min(subQueryRoot.get("id"))).where(cb.equal(subQueryRoot.get("product"), root));

        //Разделить строку, что бы получить отдельные токены диапазона
        String[] numbers = priceRange.split("[-–—_|]");

        //Если получить значения из строки удалось, тогда пытаемся их спарсить
        if (numbers.length > 1) {

            Integer priceLo = Utils.TryParseInt(numbers[0]);
            Integer priceHi = Utils.TryParseInt(numbers[1]);

            if (priceLo != null && priceHi != null)
                return cb.and(
                        cb.equal(productProductVariantJoin.get("id"), subqueryId),
                        cb.between(productProductVariantJoin.get("price"), priceLo, priceHi)
                );//cb.and
        }
        return null;
    }

    //Сформировать все предикаты для товаров
    public static List<Predicate> collectProductsPredicates(CriteriaBuilder cb, From<?, ?> root, CriteriaQuery<?> query,
                                                            ProductFilterDtoContainer container, Long categoryId, String priceRange){
        Class<?> rootType = root.getJavaType();

        if (rootType != Product.class)
            return null;

        List<Predicate> predicates = new ArrayList<>();

        //Доп.фильтрация по категории
        if (categoryId != null) {
            //Присоединить сущность категорий
            Join<Product, Category> categoryJoin = root.join("category");

            //Задать доп. условие выборки - по категориям
            predicates.add(cb.equal(categoryJoin.get("id"), categoryId));
        }

        //Доп.фильтрация по производителям
        if (container != null && container.getProducersNames() != null && container.getProducersNames().size() > 0){
            Join<Product, Producer> producerJoin = root.join("producer");

            predicates.add(producerJoin.get("producerName").in(container.getProducersNames()));
        }

        //Доп.фильтрация по ценам
        if (priceRange != null)
            predicates.add(getPricePredicate(priceRange, root, query, cb));

        return predicates;
    }

    // Сформировать все предикаты для заказов
    public static List<Predicate> collectOrdersPredicates(CriteriaBuilder cb, OrdersAndBasketsCountFiltersRequestDto dto,
                                                          Path<Product> productPath, Path<Order> orderPath, Path<ProductVariant> pvPath){

        List<Predicate> predicates = new ArrayList<>();

        if (dto.getCategoryId() != null && dto.getCategoryId() > 0)
            predicates.add(cb.equal(productPath.get("category").get("id"), dto.getCategoryId()));

        // Если задан статус заказа
        if (dto.getStateId() != null && dto.getStateId() > 0)
            predicates.add(cb.equal(orderPath.get("orderState").get("id"), dto.getStateId()));


        //region Используются именно отдельные проверки условий вместо between, чтобы можно было задать начальное значение без конечного
        // Если задана начальная дата
        if (dto.getMinDate() != null)
            predicates.add(cb.greaterThanOrEqualTo(orderPath.get("orderDate"), dto.getMinDate()));

        // Если задана конечная дата
        if (dto.getMaxDate() != null)
            predicates.add(cb.lessThanOrEqualTo(orderPath.get("orderDate"), dto.getMaxDate()));

        // Если задана минимальная цена
        if (dto.getPriceMin() != null)
            predicates.add(cb.greaterThanOrEqualTo(pvPath.get("price"), dto.getPriceMin()));

        // Если задана максимальная цена варианта
        if (dto.getPriceMax() != null)
            predicates.add(cb.lessThanOrEqualTo(pvPath.get("price"), dto.getPriceMax()));
        //endregion

        return predicates;
    }

    // Сформировать все предикаты для корзин
    public static List<Predicate> collectBasketsPredicates(CriteriaBuilder cb, OrdersAndBasketsCountFiltersRequestDto dto,
                                                          Path<Product> productPath, Path<Basket> basketPath, Path<ProductVariant> pvPath){

        List<Predicate> predicates = new ArrayList<>();

        if (productPath != null && dto.getCategoryId() != null && dto.getCategoryId() > 0)
            predicates.add(cb.equal(productPath.get("category").get("id"), dto.getCategoryId()));

        //region Используются именно отдельные проверки условий вместо between, чтобы можно было задать начальное значение без конечного
        // Если задана начальная дата
        if (basketPath != null && dto.getMinDate() != null)
            predicates.add(cb.greaterThanOrEqualTo(basketPath.get("addedDate"), dto.getMinDate()));

        // Если задана конечная дата
        if (basketPath != null && dto.getMaxDate() != null)
            predicates.add(cb.lessThanOrEqualTo(basketPath.get("addedDate"), dto.getMaxDate()));

        // Если задана минимальная цена
        if (pvPath != null && dto.getPriceMin() != null)
            predicates.add(cb.greaterThanOrEqualTo(pvPath.get("price"), dto.getPriceMin()));

        // Если задана максимальная цена варианта
        if (pvPath != null && dto.getPriceMax() != null)
            predicates.add(cb.lessThanOrEqualTo(pvPath.get("price"), dto.getPriceMax()));
        //endregion

        return predicates;
    }

    // Найти заказы по id или по коду через criteria api
    public static Order getOrdersByIdOrCode(Long id, Long orderCode, EntityManager entityManager) {

        if (id == null && orderCode == null)
            return null;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Для формирования запросов
        CriteriaQuery<Order> query = cb.createQuery(Order.class);

        // Таблица со заказываемыми товарами
        Root<Order> root = query.from(Order.class);

        Predicate selectionPredicate;

        // Сформировать условия в зависимости от параметров
        if (id == null)
            selectionPredicate = cb.equal(root.get("code"), orderCode);
        else if (orderCode == null)
            selectionPredicate = cb.equal(root.get("id"), id);
        else
            selectionPredicate = cb.and(
                    cb.equal(root.get("id"), id),
                    cb.equal(root.get("code"), orderCode)
            );

        query.where(selectionPredicate);

        return entityManager.createQuery(query).getSingleResult();
    }

    // Найти варианты товаров по id или по коду заказа через criteria api
    public static OrderAndProductVariant getOrdersAndPvByIdOrCode(Long id, Long orderCode, EntityManager entityManager) {

        if (id == null && orderCode == null)
            return null;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Для формирования запросов
        CriteriaQuery<OrderAndProductVariant> query = cb.createQuery(OrderAndProductVariant.class);

        // Таблица со заказываемыми товарами
        Root<OrderAndProductVariant> root = query.from(OrderAndProductVariant.class);

        // Присоединить заказы
        Path<Order> orderPath = root.get("order");

        Predicate selectionPredicate;

        // Сформировать условия в зависимости от параметров
        if (id == null)
            selectionPredicate = cb.equal(orderPath.get("code"), orderCode);
        else if (orderCode == null)
            selectionPredicate = cb.equal(orderPath.get("id"), id);
        else
            selectionPredicate = cb.and(
                    cb.equal(orderPath.get("id"), id),
                    cb.equal(orderPath.get("code"), orderCode)
            );

        query.where(selectionPredicate);

        return entityManager.createQuery(query).getSingleResult();
    }

    // Метод для поиска корзин - частично обобщённый
    public static <R> R findByProdVariantIdAndUserIdGeneric(Long pvId, List<Long> pvIdList, Integer userId, EntityManager entityManager, /*Class<Q> queriesType,*/ Class<R> returnType){

        if (pvId == null && userId == null && pvIdList == null)
            return null;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        //Объект для формирования запросов к БД
        CriteriaQuery<Basket> query = cb.createQuery(Basket.class);

        //Составная таблица - корзина
        Root<Basket> root = query.from(Basket.class);

        //Присоединить таблицу многие ко многим и вариантов товара
        Join<Basket, BasketAndProductVariant> bpvJoin = pvId != null || pvIdList != null ? root.join("basketAndPVList") : null;
        Path<ProductVariant> productVariantPath = bpvJoin != null ? bpvJoin.get("productVariant") : null;

        Join<Basket, User> userJoin = userId != null ? root.join("user") : null;

        //Условие для выборки если заданы оба параметра
        Predicate predicate = pvId != null && userId != null ? cb.and(
                cb.equal(productVariantPath.get("id"), pvId),
                cb.equal(userJoin.get("id"), userId)
        ) : null;

        // Если не все параметры заданы, только id варианта или пользователя, либо если задан только список
        if (pvId != null && userId == null)
            predicate = cb.equal(productVariantPath.get("id"), pvId);
        else if (userId != null && pvId == null)
            predicate = cb.equal(userJoin.get("id"), userId);
        else if (pvIdList != null) {

            CriteriaBuilder.In<Long> cbIn =  cb.in(productVariantPath.get("id"));

            // Добавить значения из списка в условие
            pvIdList.forEach(cbIn::value);

            predicate = cbIn;
        }

        query.where(predicate);

        List<Basket> resultList = entityManager.createQuery(query).getResultList();

        // Небезопасные приведения выбраны осознанно
        if (resultList.size() > 0 && List.class.isAssignableFrom(returnType))
            return (R) resultList;
        else if (resultList.size() > 0 && returnType == Basket.class)
            return (R) resultList.get(0);

        return null;
    }

    // Пересчёт суммы в корзине
    public static void countSumInBaskets(List<Basket> baskets, List<BasketAndProductVariant> bpvListAll){
        List<BasketAndProductVariant> bpvList;

        // Для каждой корзины пересчитать сумму
        for (Basket basket: baskets) {
            // Выбрать записи для текущей корзины и записи, где вариант товара не скрыт и не удалён
            bpvList = bpvListAll.stream()
                    .filter(e -> e.getBasket().getId().equals(basket.getId())
                            && e.getProductVariant().getShowVariant()
                            && (e.getProductVariant().getIsDeleted() == null || !e.getProductVariant().getIsDeleted()))
                    .toList();

            if (bpvList.isEmpty())
                continue;

            // Пересчитать сумму с изменённым вариантом товара
            int newSum = bpvList.stream()
                    .map(bpv -> bpv.getProductVariant().getPrice() * bpv.getProductsAmount())
                    .reduce(0, Integer::sum);

            basket.setSum(newSum);

        }
    }

    // Пересчёт суммы в заказах
    public static void countSumInOrders(List<Order> orders, List<OrderAndProductVariant> opvListAll){
        List<OrderAndProductVariant> opvList;

        for(Order order: orders){
            // Специально не производим никаких других проверок, поскольку для заказов с разными статусами могут быть нужны
            // разные расчёты, которые будут определятся за пределом данного метода
            opvList = opvListAll.stream()
                    .filter(e -> e.getOrder().getId().equals(order.getId()))
                    .toList();

            if (opvList.isEmpty())
                continue;

            // Собственно пересчёт суммы
            int newSum = opvList.stream()
                    .map(e -> e.getProductVariant().getPrice() * e.getProductsAmount())
                    .reduce(0, Integer::sum);

            order.setSum(newSum);

        }// for
    }

    // Пересчёт суммы в одном заказе
    public static void countSumInOrder(Order order, List<OrderAndProductVariant> opvList, boolean filtrateList){

        // Если задан флаг фильтрации
        if (filtrateList)
            opvList = opvList.stream()
                    .filter(e ->
                            e.getProductVariant().getShowVariant() &&
                            (e.getProductVariant().getIsDeleted() == null || !e.getProductVariant().getIsDeleted())
                    ).toList();

        if (opvList.isEmpty())
            return;

        // Собственно пересчёт суммы
        int newSum = opvList.stream()
                .map(e -> e.getProductVariant().getPrice() * e.getProductsAmount())
                .reduce(0, Integer::sum);

        order.setSum(newSum);

    }

    // Формирование запроса для получения ProductsViews
    public static TypedQuery<Tuple> getTypedQueryProductsViews(AggregateOperationsEnum operation, EntityManager entityManager, Long categoryId, String priceRange, GeneralSortEnum sortEnum){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
        Root<ProductViews> root = query.from(ProductViews.class);

        Join<ProductViews, Product> productJoin = root.join("product", JoinType.LEFT);

        // Сформировать запрос с агрегатной функцией подсчёта суммы
        Expression<?> expression/* = cb.sum(cb.coalesce(root.get("count"), 0))*/ = switch (operation) {
            case SUM -> cb.sum(cb.coalesce(root.get("count"), 0));
            case AVG -> cb.avg(cb.coalesce(root.get("count"), 0));
            case MAX -> cb.max(cb.coalesce(root.get("count"), 0));
            case MIN -> cb.min(cb.coalesce(root.get("count"), 0));
            default -> cb.count(root.get("id"));
        };

        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, productJoin, query, null, categoryId, priceRange);

        if (predicates != null && !predicates.isEmpty())
            query.where(predicates.toArray(new Predicate[0]));

        query.multiselect(
                productJoin.get("id"),
                expression
        ).groupBy(productJoin.get("id"));

        query.orderBy(sortEnum == GeneralSortEnum.ASC ? cb.asc(expression) : cb.desc(expression));

        return entityManager.createQuery(query);
    }

    // Подсчёт количества записей просмотров товаров с заданными параметрами
    public static Long getTypedQueryCountProductsViews(EntityManager entityManager, Long categoryId, String priceRange){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ProductViews> root = query.from(ProductViews.class);

        Join<ProductViews, Product> productJoin = root.join("product", JoinType.LEFT);

        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, productJoin, query, null, categoryId, priceRange);

        if (predicates != null && !predicates.isEmpty())
            query.where(predicates.toArray(new Predicate[0]));

        query.select(cb.countDistinct(productJoin.get("id")))/*.groupBy(productJoin.get("id"))*/;

        return entityManager.createQuery(query).getResultList().get(0);
    }

    // Подсчёт количества посетителей, которые просмотрели товары с заданными параметрами
    public static Long countVisitorsWithProductsViews(EntityManager entityManager, Long categoryId, String priceRange){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Visitor> root = query.from(Visitor.class);

        Join<Visitor, ProductViews> productViewsJoin = root.join("productViewsList");
        Join<ProductViews, Product> productJoin = productViewsJoin.join("product");

        // Сформировать запрос

        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, productJoin, query, null, categoryId, priceRange);

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
        List<Predicate> predicates = collectProductsPredicates(cb, productJoin, query, null, categoryId, priceRange);

        // Основной запрос
        Expression<Integer> sumExpression = cb.sum(cb.coalesce(root.get("count"), 0));

        if (predicates != null && !predicates.isEmpty())
            query.where(predicates.toArray(new Predicate[0]));

        query.select(productJoin.get("id"))
                .groupBy(productJoin.get("id"))
                .having(cb.ge(sumExpression, maxCount));


        return entityManager.createQuery(query).getResultList().size();
    }

    // Подсчёт общего просмотренных товаров для одного покупателя
    public static long countCustomerProductsViews(EntityManager entityManager, CustomerRequestDto customerDto){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Основной запрос выборки записей просмотров товаров
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ProductViews> root = query.from(ProductViews.class);
        Path<Visitor> visitorPath = root.get("visitor");
        Path<Customer> customerPath = visitorPath.get("customers");

        Predicate predicate;
        if (customerDto.getId() != null && customerDto.getFingerPrint() != null)
            predicate = cb.or(
                    cb.equal(visitorPath.get("fingerprint"), customerDto.getFingerPrint()),
                    cb.equal(customerPath.get("id"), customerDto.getId())
            );
        else if (customerDto.getId() == null)
            predicate = cb.equal(visitorPath.get("fingerprint"), customerDto.getFingerPrint());
        else
            predicate = cb.equal(customerPath.get("id"), customerDto.getId());

        query.where(predicate);
        query.select(cb.count(root.get("id")));

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);

        typedQuery.setMaxResults(1);

        return typedQuery.getSingleResult();
    }


}