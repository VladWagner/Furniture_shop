package gp.wagner.backend.infrastructure;

import gp.wagner.backend.domain.dto.request.admin_panel.DatesRangeRequestDto;
import gp.wagner.backend.domain.dto.request.admin_panel.OrdersAndBasketsCountFiltersRequestDto;
import gp.wagner.backend.domain.dto.request.crud.CustomerRequestDto;
import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.dto.response.filters.FilterValuesDto;
import gp.wagner.backend.domain.entites.baskets.Basket;
import gp.wagner.backend.domain.entites.baskets.BasketAndProductVariant;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.eav.ProductAttribute;
import gp.wagner.backend.domain.entites.orders.Customer;
import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.entites.orders.OrderAndProductVariant;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.domain.entites.visits.DailyVisits;
import gp.wagner.backend.domain.entites.visits.ProductViews;
import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.infrastructure.enums.AggregateOperationsEnum;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.ProductsOrVariantsEnum;
import gp.wagner.backend.middleware.Services;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;
import java.util.stream.Collectors;

//Класс для вынесения повторяющихся и вспомогательных методов из сервисов
public class ServicesUtils {

    //Создание предиката для фильтрации товаров по цене
    public static Predicate getProductPricePredicate(SimpleTuple<Integer, Integer> priceRange, From<?, ?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        //Присоединить таблицу вариантов товаров
        Join<Product, ProductVariant> productProductVariantJoin = root.join("productVariants");

        //Создание подзапроса для получения мин.id варианта товара - базового варианта товара
        Subquery<Long> subqueryId = query.subquery(Long.class);
        Root<ProductVariant> subQueryRoot = subqueryId.from(ProductVariant.class);
        subqueryId.select(cb.min(subQueryRoot.get("id"))).where(cb.equal(subQueryRoot.get("product"), root));


        //Если получить значения из строки удалось, тогда пытаемся их спарсить
        if (priceRange != null) {

            int priceLo = priceRange.getValue1();
            int priceHi = priceRange.getValue2();

            return cb.and(
                    cb.equal(productProductVariantJoin.get("id"), subqueryId),
                    cb.between(productProductVariantJoin.get("price"), priceLo, priceHi)
            );//cb.and
        }
        return null;
    }
    public static Predicate getProductVariantPricePredicate(SimpleTuple<Integer, Integer> priceRange, From<?, ?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        //Присоединить таблицу вариантов товаров
        Join<Product, ProductVariant> productAndPvJoin = root.join("productVariants");

        //Создание подзапроса для получения вариантов для текущего товара, которые входят в диапазон
        Subquery<Long> subQueryId = query.subquery(Long.class);
        Root<ProductVariant> subQueryRoot = subQueryId.from(ProductVariant.class);

        // Подзапрос для проверки базового варианта (его цена >= нижней границе) для корректного вывода
        Subquery<Long> baseVpSubQuery = query.subquery(Long.class);
        Root<ProductVariant> baseVpRoot = baseVpSubQuery.from(ProductVariant.class);

        //Если получить значения из строки удалось, тогда формируем предикаты
        if (priceRange != null) {

            int priceLo = priceRange.getValue1();
            int priceHi = priceRange.getValue2();

            // Принимать в расчёт только выводимые и неудалённые варианты для определённого товара
            subQueryId.select(subQueryRoot.get("product").get("id"))
                    .where(cb.and(
                            cb.equal(subQueryRoot.get("product").get("id"), root.get("id")),
                            cb.between(subQueryRoot.get("price"), priceLo, priceHi),
                            cb.equal(subQueryRoot.get("showVariant"), true),
                            cb.equal(subQueryRoot.get("isDeleted"), false)
                           )
                    );

            // Найти id базового варианта
            //baseVpSubQuery.select(cb.min(subQueryRoot.get("id"))).where(cb.equal(baseVpRoot.get("product"), root));

            // return cb.in(root.get("id")).value(subQueryId);
            return cb.and(
                    //cb.ge(productAndPvJoin.get("price"), priceLo),
                    cb.in(root.get("id")).value(subQueryId)
            );

        }
        return null;
    }

    /**
     * Метод формирует предикаты для параметров, которые на заданы в таблице products_attributes
     * и являются свойствами Product или ProductVariant.
     * @param root собственно источник выборки. Задан именно в типе From<?,?>, чтобы можно было задавать Path<> и Join<> в различных вызовах.
     *
     * @param povEnum флаг для определения типа фильрации: по цене товара или его вариантов.
     *                Т.е. при проверке цены на принадлежность к диапазону будет происходить проверка только цены базового варианта или всех вариантов товара
     * */
    public static List<Predicate> collectProductsPredicates(CriteriaBuilder cb, From<?, ?> root, CriteriaQuery<?> query,
                                                            ProductFilterDtoContainer container, Long categoryId, String priceRange, ProductsOrVariantsEnum povEnum){
        Class<?> rootType = root.getJavaType();

        if (rootType != Product.class)
            return null;

        List<Predicate> predicates = new ArrayList<>();

        // Доп.фильтрация по категории
        if (categoryId != null) {
            // Присоединить сущность категорий
            Join<Product, Category> categoryJoin = root.join("category");

            // Получить id дочерних категорий
            List<Long> childCategoriesIds = getChildCategoriesList(categoryId);

            // Выбрать товары, которые содержатся в дочерних категориях, если задана родительская
            predicates.add(categoryJoin.get("id").in(childCategoriesIds));
        }

        // Доп.фильтрация по производителям
        if (container != null && container.getProducersNames() != null && container.getProducersNames().size() > 0){
            Join<Product, Producer> producerJoin = root.join("producer");

            predicates.add(producerJoin.get("producerName").in(container.getProducersNames()));
        }

        // Доп.фильтрация по ценам товара или вариантов
        if (priceRange != null && povEnum != null) {

            SimpleTuple<Integer, Integer> rangeTuple = Utils.parseTwoNumericValues(priceRange);

            // Если получить диапазон не вышло, тогда уходим
            if (rangeTuple == null)
                return predicates;

            // Определить тип выборки по цене (базового варианта или всех вариантов товара)
            predicates.add(
                    povEnum == ProductsOrVariantsEnum.PRODUCTS ?
                    getProductPricePredicate(rangeTuple, root, query, cb) :
                    getProductVariantPricePredicate(rangeTuple, root, query, cb)
            );
        }

        return predicates;
    }

    public static List<Long> getChildCategoriesList(Long categoryId){

        // Если задана id обычной категории (по договоренности значения < 0 могут быть id повторяющихся категорий)
        if (categoryId > 0)
            return Services.categoriesService.getAllChildCategories(categoryId);

        categoryId = Math.abs(categoryId);

        // Получить id категорий, использующих данную повторяющуюся категорию
        List<Long> categoriesIds = Services.categoriesService.getRepeatingCategoryChildren(categoryId);

        // Результирующая рекурсивная выборка id дочерних категорий
        List<Long> resultCategoriesList = new ArrayList<>(Services.categoriesService.getAllChildCategories(categoriesIds));

        return resultCategoriesList;
    }

    public static List<Long> getChildCategoriesList(List<Long> categoryIds){

        if (categoryIds == null || categoryIds.isEmpty())
            return null;

        List<Long> simpleCategoriesIds = categoryIds.stream().filter(e -> e > 0).toList();

        // Оставить только id повторяющихся категорий (по договоренности их id < 0)
        List<Long> repeatingCategoriesIds = categoryIds.stream().filter(e -> e < 0).collect(Collectors.toCollection(ArrayList::new));

        List<Long> resultCategoriesList = new ArrayList<>();

        // Если не пуст список обычных категорий
        if (!simpleCategoriesIds.isEmpty())
            resultCategoriesList.addAll(Services.categoriesService.getAllChildCategories(simpleCategoriesIds));

        // Если в основном списке остались категории <= 0 - id записей из таблицы повторяющихся категорий
        if (!repeatingCategoriesIds.isEmpty()) {
            repeatingCategoriesIds.replaceAll(Math::abs);

            // Получить категории, использующие найденные повторяющиеся категории + дочерние категории данных категорий
            resultCategoriesList.addAll(Services.categoriesService.getRepeatingCategoriesChildrenWithHeirs(repeatingCategoriesIds));
        }

        return resultCategoriesList;
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
            /*int newSum = opvList.stream()
                    .map(e -> e.getProductVariant().getPrice() * e.getProductsAmount())
                    .reduce(0, Integer::sum);*/
            int newSum = 0;
            int newProductsAmount = 0;

            for (OrderAndProductVariant opv : opvList) {
                newSum += opv.getProductVariant().getPrice() * opv.getProductsAmount();
                newProductsAmount += opv.getProductsAmount();
            }

            order.setSum(newSum);
            order.setGeneralProductsAmount(newProductsAmount);

        }// for
    }

    // Пересчёт суммы в одном заказе
    public static void countSumInOrder(Order order, List<OrderAndProductVariant> opvList, boolean filtrateOpvList){

        // Если задан флаг фильтрации
        if (filtrateOpvList)
            opvList = opvList.stream()
                    .filter(e ->
                            e.getProductVariant().getShowVariant() &&
                            (e.getProductVariant().getIsDeleted() == null || !e.getProductVariant().getIsDeleted())
                    ).toList();

        if (opvList.isEmpty())
            return;

        // Собственно пересчёт суммы
            /*int newSum = opvList.stream()
                    .map(e -> e.getProductVariant().getPrice() * e.getProductsAmount())
                    .reduce(0, Integer::sum);*/
        int newSum = 0;
        int newProductsAmount = 0;

        // Посчитать
        for (OrderAndProductVariant opv : opvList) {
            newSum += opv.getProductVariant().getPrice() * opv.getProductsAmount();
            newProductsAmount += opv.getProductsAmount();
        }

        order.setSum(newSum);
        order.setGeneralProductsAmount(newProductsAmount);

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

        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, productJoin, query, null, categoryId, priceRange, ProductsOrVariantsEnum.PRODUCTS);

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

        List<Predicate> predicates = ServicesUtils.collectProductsPredicates(cb, productJoin, query, null, categoryId, priceRange,
                ProductsOrVariantsEnum.PRODUCTS);

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
        List<Predicate> predicates = collectProductsPredicates(cb, productJoin, query, null, categoryId, priceRange,
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

    // Подсчёт общего количества просмотренных товаров для одного покупателя
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

    public static long countProductsByFilter(EntityManager entityManager,List<Specification<Product>> specifications, ProductFilterDtoContainer container, Long categoryId, String priceRange){

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

        List<Predicate> predicates = collectProductsPredicates(cb, root, query, container, categoryId, priceRange, ProductsOrVariantsEnum.VARIANTS);

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

    // Сформировать и отсортировать ассоциативную коллекцию фильтров по убыванию приоритетов
    public static Map<String, List<FilterValuesDto<Integer>>> createAndSortFilterMap(List<FilterValuesDto<Integer>> filterValuesDtoList){

        // Сформировать начальную ассоциативную коллекцию без сортировки по приоритетам
        Map<String, List<FilterValuesDto<Integer>>> groupedFilters = filterValuesDtoList.stream()
                .collect(Collectors.groupingBy(FilterValuesDto::getAttributeName));

        // Новый отсортированный map

        return groupedFilters.entrySet().
                stream()
                .sorted(Comparator.comparing(entry -> entry.getValue().get(0).getPriority()) )
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
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

    // Подсчёт количества товаров для определённого производителя
    public static long countProductsByProducerOrCategory(EntityManager entityManager, long requiredId, Class<?> searchingType){

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


}