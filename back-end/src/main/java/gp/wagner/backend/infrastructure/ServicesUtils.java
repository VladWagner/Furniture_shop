package gp.wagner.backend.infrastructure;

import gp.wagner.backend.domain.dto.request.admin_panel.OrdersAndBasketsCountFiltersRequestDto;
import gp.wagner.backend.domain.dto.request.filters.CustomersFilterRequestDto;
import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.dto.response.filters.FilterValuesDto;
import gp.wagner.backend.domain.entities.baskets.Basket;
import gp.wagner.backend.domain.entities.baskets.BasketAndProductVariant;
import gp.wagner.backend.domain.entities.categories.Category;
import gp.wagner.backend.domain.entities.orders.Customer;
import gp.wagner.backend.domain.entities.orders.Order;
import gp.wagner.backend.domain.entities.orders.OrderAndProductVariant;
import gp.wagner.backend.domain.entities.products.Discount;
import gp.wagner.backend.domain.entities.products.Producer;
import gp.wagner.backend.domain.entities.products.Product;
import gp.wagner.backend.domain.entities.products.ProductVariant;
import gp.wagner.backend.domain.entities.users.User;
import gp.wagner.backend.domain.entities.visits.ProductViews;
import gp.wagner.backend.infrastructure.enums.AggregateOperationsEnum;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.ProductsOrVariantsEnum;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.security.models.UserDetailsImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import java.util.*;
import java.util.stream.Collectors;

//Класс для вынесения повторяющихся и вспомогательных методов из сервисов
public class ServicesUtils {

    //Создание предиката для фильтрации товаров по цене
    public static Predicate getProductPricePredicate(SimpleTuple<Integer, Integer> priceRange, From<?, ?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        //Присоединить таблицу вариантов товаров
        Join<Product, ProductVariant> productProductVariantJoin = root.join("productVariants");
        Join<ProductVariant, Discount> discountJoin = productProductVariantJoin.join("discount", JoinType.LEFT);

        //Создание подзапроса для получения мин.id варианта товара - базового варианта товара
        Subquery<Long> subqueryId = query.subquery(Long.class);
        Root<ProductVariant> subQueryRoot = subqueryId.from(ProductVariant.class);
        subqueryId.select(cb.min(subQueryRoot.get("id"))).where(cb.equal(subQueryRoot.get("product"), root));


        //Если получить значения из строки удалось, тогда пытаемся их спарсить
        if (priceRange != null) {

            int priceLo = priceRange.getValue1();
            int priceHi = priceRange.getValue2();

            // Проверка наличия скидки и цены варианта с ней
            Predicate discountPriceInRange = cb.and(
                    cb.isNotNull(discountJoin.get("id")),
                    cb.between(cb.diff(
                            productProductVariantJoin.get("price"),
                            cb.prod(productProductVariantJoin.get("price"), discountJoin.get("percentage"))
                    ), priceLo, priceHi)
            );

            // Если скидка не задана, тогда сравнение с обычной ценой
            Predicate regularPriceInRange = cb.between(productProductVariantJoin.get("price"), priceLo, priceHi);

            return cb.and(
                    cb.equal(productProductVariantJoin.get("id"), subqueryId),
                    cb.or(discountPriceInRange, regularPriceInRange)
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
        Join<ProductVariant, Discount> discountJoin = subQueryRoot.join("discount", JoinType.LEFT);

        // Подзапрос для проверки базового варианта (его цена >= нижней границе) для корректного вывода
        Subquery<Long> baseVpSubQuery = query.subquery(Long.class);
        Root<ProductVariant> baseVpRoot = baseVpSubQuery.from(ProductVariant.class);

        // Если получить значения из строки удалось, тогда формируем предикаты
        if (priceRange != null) {

            int priceLo = priceRange.getValue1();
            int priceHi = priceRange.getValue2();

            // Проверка наличия скидки и цены варианта с ней
            Predicate discountPriceInRange = cb.and(
                    cb.isNotNull(discountJoin.get("id")),
                    cb.between(cb.diff(
                            subQueryRoot.get("price"),
                            cb.prod(subQueryRoot.get("price"), discountJoin.get("percentage"))
                    ), priceLo, priceHi)
            );

            // Если скидка не задана, тогда сравнение с обычной ценой
            Predicate regularPriceInRange = cb.between(subQueryRoot.get("price"), priceLo, priceHi);

            // Принимать в расчёт только выводимые и неудалённые варианты для определённого товара
            subQueryId.select(subQueryRoot.get("product").get("id"))
                    .where(cb.and(
                            cb.equal(subQueryRoot.get("product").get("id"), root.get("id")),
                            //cb.between(subQueryRoot.get("price"), priceLo, priceHi),
                            cb.or(discountPriceInRange, regularPriceInRange),
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

        // Оставить только id категорий из основной таблицы
        List<Long> simpleCategoriesIds = categoryIds.stream().filter(e -> e > 0).toList();

        // Оставить только id повторяющихся категорий (по договоренности их id < 0)
        List<Long> repeatingCategoriesIds = categoryIds.stream()
                .filter(e -> e < 0)
                .collect(Collectors.toCollection(ArrayList::new));

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
                                                          Path<Product> productPath, Path<Order> orderPath, From<?,OrderAndProductVariant> opvFrom){

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
            //predicates.add(cb.greaterThanOrEqualTo(pvPath.get("price"), dto.getPriceMin()));
            predicates.add(
                    cb.and(
                            cb.isNotNull(opvFrom.get("unitPrice")),
                            cb.greaterThanOrEqualTo(opvFrom.get("unitPrice"), dto.getPriceMin())
                    )
            );

        // Если задана максимальная цена варианта
        if (dto.getPriceMax() != null)
            //predicates.add(cb.lessThanOrEqualTo(pvPath.get("price"), dto.getPriceMax()));
            predicates.add(
                    cb.and(
                            cb.isNotNull(opvFrom.get("unitPrice")),
                            cb.lessThanOrEqualTo(opvFrom.get("unitPrice"), dto.getPriceMax())
                    )
            );
        //endregion

        return predicates;
    }

    // Сформировать все предикаты для корзин
    public static List<Predicate> collectBasketsPredicates(CriteriaBuilder cb, OrdersAndBasketsCountFiltersRequestDto dto,
                                                          Path<Product> productPath, Path<Basket> basketPath, From<?, ProductVariant> pvFrom){

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

        // Фильтрация по цене с учётом наличия скидки на вариант товара
        if (pvFrom != null && (dto.getPriceMin() != null || dto.getPriceMax() != null)) {
            Join<ProductVariant, Discount> discountJoin = pvFrom.join("discount", JoinType.LEFT);
            Expression<Integer> discountPrice = cb.diff(
                    pvFrom.get("price"),
                    cb.prod(pvFrom.get("price"), discountJoin.get("percentage"))
            );
            Predicate discountPricePredicate = cb.isNotNull(discountJoin.get("id"));

            // Если задана минимальная цена
            if (dto.getPriceMin() != null) {
                // Скидка должна быть задана либо произойдёт проверка по штатной цене варианта
                discountPricePredicate = cb.and(
                        discountPricePredicate,
                        cb.greaterThanOrEqualTo(discountPrice, dto.getPriceMin())
                );
                predicates.add(
                        cb.or(
                                discountPricePredicate,
                                cb.greaterThanOrEqualTo(pvFrom.get("price"), dto.getPriceMin())
                        )
                );
            }

            // Если задана максимальная цена варианта
            if (dto.getPriceMax() != null)
            {
                // Скидка должна быть задана либо произойдёт проверка по штатной цене варианта
                discountPricePredicate = cb.and(
                        discountPricePredicate,
                        cb.lessThanOrEqualTo(discountPrice, dto.getPriceMax())
                );
                predicates.add(
                        cb.or(
                                discountPricePredicate,
                                cb.lessThanOrEqualTo(pvFrom.get("price"), dto.getPriceMax())
                        )
                );
            }
        }
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

    // Метод для поиска корзин - частично обобщённый (возвращает либо единичную корзину, либо коллекцию корзин)
    public static <R> R findBasketByProdVariantIdAndUserIdGeneric(Long pvId, List<Long> pvIdList, Integer userId, EntityManager entityManager, /*Class<Q> queriesType,*/ Class<R> returnType){

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
                            && !e.getProductVariant().getIsDeleted())
                    .toList();

            if (bpvList.isEmpty())
                continue;

            // Пересчитать сумму с изменённым вариантом товара
            int newSum = bpvList.stream()
                    .map(bpv -> bpv.getProductVariant().getPriceWithDiscount() * bpv.getProductsAmount())
                    .reduce(0, Integer::sum);

            basket.setSum(newSum);

        }
    }
    public static void countSumInBasket(Basket basket){

        if (basket == null || basket.getBasketAndPVList().isEmpty())
            return;

        // Пересчитать сумму по заданным вариантам товаров и их кол-ву
        int newSum = basket.getBasketAndPVList().stream()
                .map(bpv -> bpv.getProductVariant().getPriceWithDiscount() * bpv.getProductsAmount())
                .reduce(0, Integer::sum);

        basket.setSum(newSum);
    }

    // Пересчёт суммы в заказах
    public static void countSumInOrders(List<Order> orders, List<OrderAndProductVariant> opvListAll){
        List<OrderAndProductVariant> opvList;

        for(Order order: orders){
            // Специально не производим никаких других проверок, поскольку для заказов с разными статусами могут быть нужны
            // разные расчёты, которые будут определяться за пределом данного метода
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
                // Зафиксировать стоимость за единицу товара на момент пересчёта суммы
                opv.setUnitPrice(opv.getProductVariant().getPriceWithDiscount());

                // Получаем стоимость со скидкой, в любом случае значение будет корректным
                //newSum += opv.getProductVariant().getPriceWithDiscount() * opv.getProductsAmount();
                newSum += opv.getUnitPrice() * opv.getProductsAmount();
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
            newSum += opv.getProductVariant().getPriceWithDiscount() * opv.getProductsAmount();
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

        // Сформировать запрос с агрегатной для колчества просмотров каждого товвара
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

    // Сформировать и отсортировать ассоциативную коллекцию фильтров по убыванию приоритетов
    public static Map<String, List<FilterValuesDto<Integer>>> createAndSortFiltersMap(List<FilterValuesDto<Integer>> filterValuesDtoList){

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

    // Получить пользователя из SecurityContext
    public static User getUserFromSecurityContext(SecurityContext securityContext){

        Authentication authentication = securityContext.getAuthentication();

        if (authentication == null)
            return null;

        Object principal = authentication.getPrincipal();

        // Если в authentication задан полностью пользователь, тогда вернуть его
        if (User.class.isAssignableFrom(principal.getClass()))
            return (User) principal;

        long userId = ((UserDetailsImpl) principal).getUserId();

        return Services.usersService.getById(userId);

    }

    public static Expression<String> customerSnpExpression(CriteriaBuilder cb, From<Customer, ?> customer){

        Expression<Integer> digit  = cb.literal(1);
        return cb.function("CONCAT_WS", String.class, cb.literal("."),
                customer.get("name"),
                cb.function("SUBSTR", String.class, customer.get("surname"), digit, digit),
                cb.function("SUBSTR", String.class, customer.get("patronymic"), digit, digit)
        );
    }

    // Сформировать все предикаты для фильтрации покупателей
    public static List<Predicate> collectCustomersPredicates(CriteriaBuilder cb, From<Customer, ?> customer, CustomersFilterRequestDto filterDto,
                                                             Expression<Boolean> isRegisteredExp, Expression<Long> ordersCountExp,
                                                             Expression<Integer> ordersUnitsCountExp, Expression<Integer> ordersSumsExp,
                                                             Expression<Double> avgUnitPrice){

        List<Predicate> predicates = new ArrayList<>();

        if (cb == null || customer == null)
            return predicates;

        // Является ли покупатель зарегистрированным пользователем
        if (filterDto.getIsRegistered() != null)
            predicates.add(cb.equal(isRegisteredExp, filterDto.getIsRegistered()));

        // Даты создания записи
        if (filterDto.getMinDate() != null)
            predicates.add(cb.greaterThanOrEqualTo(customer.get("createdAt"), filterDto.getMinDate()));

        if (filterDto.getMaxDate() != null)
            predicates.add(cb.lessThanOrEqualTo(customer.get("createdAt"), filterDto.getMaxDate()));

        // Количество заказов сделанных покупателем
        if (filterDto.getMinOrdersCount() != null)
            predicates.add(cb.greaterThanOrEqualTo(ordersCountExp, filterDto.getMinOrdersCount()));

        if (filterDto.getMaxOrdersCount() != null)
            predicates.add(cb.lessThanOrEqualTo(ordersCountExp, filterDto.getMaxOrdersCount()));

        // Количество всех товаров в заказах сделанных покупателем
        if (filterDto.getMinOrderedUnitsCount() != null)
            predicates.add(cb.greaterThanOrEqualTo(ordersUnitsCountExp, filterDto.getMinOrderedUnitsCount()));

        if (filterDto.getMaxOrderedUnitsCount() != null)
            predicates.add(cb.lessThanOrEqualTo(ordersUnitsCountExp, filterDto.getMaxOrderedUnitsCount()));

        // Сумма всех заказов сделанных покупателем
        if (filterDto.getMinOrdersSum() != null)
            predicates.add(cb.greaterThanOrEqualTo(ordersSumsExp, filterDto.getMinOrdersSum()));

        if (filterDto.getMaxOrdersSum() != null)
            predicates.add(cb.lessThanOrEqualTo(ordersSumsExp, filterDto.getMaxOrdersSum()));

        // Средняя цена каждого товара в заказах покупателя
        if (filterDto.getMinAvgUnitPrice() != null)
            predicates.add(cb.greaterThanOrEqualTo(avgUnitPrice, filterDto.getMinAvgUnitPrice()));

        if (filterDto.getMaxAvgUnitPrice() != null)
            predicates.add(cb.lessThanOrEqualTo(avgUnitPrice, filterDto.getMaxAvgUnitPrice()));

        return predicates;
    }


}