package gp.wagner.backend.services.implementations.admin_panel;

import gp.wagner.backend.domain.dto.request.admin_panel.CustomerStatRequestDto;
import gp.wagner.backend.domain.dto.request.admin_panel.DatesRangeAndValRequestDto;
import gp.wagner.backend.domain.dto.request.admin_panel.DatesRangeRequestDto;
import gp.wagner.backend.domain.dto.request.admin_panel.OrdersAndBasketsCountFiltersRequestDto;
import gp.wagner.backend.domain.entities.baskets.Basket;
import gp.wagner.backend.domain.entities.baskets.BasketAndProductVariant;
import gp.wagner.backend.domain.entities.orders.Customer;
import gp.wagner.backend.domain.entities.orders.OrderAndProductVariant;
import gp.wagner.backend.domain.entities.products.Product;
import gp.wagner.backend.domain.entities.orders.Order;
import gp.wagner.backend.domain.entities.products.ProductVariant;
import gp.wagner.backend.domain.entities.visits.DailyVisits;
import gp.wagner.backend.domain.entities.visits.ProductViews;
import gp.wagner.backend.domain.entities.visits.Visitor;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.PaginationUtils;
import gp.wagner.backend.infrastructure.ServicesUtils;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.SortingUtils;
import gp.wagner.backend.infrastructure.enums.ProductsOrVariantsEnum;
import gp.wagner.backend.infrastructure.enums.sorting.*;
import gp.wagner.backend.infrastructure.enums.sorting.orders.OrdersStatisticsSortEnum;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.admin_panel.AdminPanelStatisticsRepository;
import gp.wagner.backend.services.interfaces.admin_panels.AdminPanelStatisticsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@Service
public class AdminPanelStatisticsServiceImpl implements AdminPanelStatisticsService {

    //Репозиторий
    private AdminPanelStatisticsRepository adminPanelRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setAdminPanelRepository(AdminPanelStatisticsRepository adminPanelRepository) {
        this.adminPanelRepository = adminPanelRepository;
    }

    // Получение кол-ва визитов по дням. Повторяет метод из DailyVisitsController
    @Override
    public Page<SimpleTuple<Date, Long>> getDailyVisitsByDatesRange(DatesRangeRequestDto datesDto, int pageNum, int dataOnPage,
                                                                    DailyVisitsSortEnum sortEnum, GeneralSortEnum sortType) throws ApiException {

        if(datesDto == null || !datesDto.isCorrect())
            throw new ApiException("Переданный DTO с диапазоном дат некорректен или дата min > max!");

        if (pageNum > 0)
            pageNum -= 1;

        Pageable pageable =  PageRequest.of(pageNum, dataOnPage, SortingUtils.createSortForDailyVisits(sortEnum, sortType));

        Page<Object[]> rawPage = adminPanelRepository.getDailyVisitsBetweenDates(datesDto.getMin(), datesDto.getMax(), pageable);

        List<SimpleTuple<Date, Long>> resultList = new ArrayList<>();

        for (Object[] rawResult: rawPage.getContent())
            resultList.add(new SimpleTuple<>((Date) rawResult[0], ((BigDecimal) rawResult[1]).longValue()));

        return new PageImpl<>(resultList, pageable, rawPage.getTotalElements());
    }

    @Override
    public long getVisitSum(DatesRangeRequestDto datesDto) {

        if(datesDto == null || !datesDto.isCorrect())
            throw new ApiException("Переданный DTO с диапазоном дат некорректен или дата min > max!");

        return adminPanelRepository.getSumDailyVisitsBetweenDates(datesDto.getMin(), datesDto.getMax());
    }

    @Override
    public Page<Object[]> getOrdersDatesRange(DatesRangeAndValRequestDto datesDto, int pageNum, int dataOnPage,
                                              OrdersStatisticsSortEnum sortEnum, GeneralSortEnum sortType) {

        if(datesDto == null || !datesDto.isCorrect())
            throw new ApiException("Переданный DTO с диапазоном дат некорректен или дата min > max!");

        if (pageNum > 0)
            pageNum -= 1;

        Pageable pageable =  PageRequest.of(pageNum, dataOnPage, SortingUtils.createSortForOrdersBetweenDates(sortEnum, sortType));

        // Получить статус заказа
        int orderStateId = datesDto.getAdditionalValues() != null ? (int) datesDto.getAdditionalValues().get("state_id") : 0;

        return adminPanelRepository.getOrdersByDaysBetweenDates(datesDto.getMin(), datesDto.getMax(), orderStateId, pageable);

    }

    @Override
    public Page<Object[]> getConversionFromViewToOrderInCategory(DatesRangeAndValRequestDto datesDto, int pageNum, int dataOnPage,
                                                                 OrdersStatisticsSortEnum sortEnum, GeneralSortEnum sortType) {

        if(datesDto == null || !datesDto.isCorrect())
            throw new ApiException("Переданный DTO с диапазоном дат некорректен или дата min > max!");

        if (pageNum > 0)
            pageNum -= 1;

        Pageable pageable = PageRequest.of(pageNum, dataOnPage, SortingUtils.createSortForOrdersCvrSelections(sortEnum, sortType));

        // Получить статус заказа
        int orderStateId = datesDto.getAdditionalValues() != null ? (int) datesDto.getAdditionalValues().get("state_id") : 0;

        // Если категория не задана, тогда будут выбраны все записи
        if (datesDto.getLongValue() == 0)
            return adminPanelRepository.getCvrToOrdersBetweenDatesInCategory(datesDto.getMin(), datesDto.getMax(), datesDto.getLongValue(),
                    orderStateId, pageable);

        List<Long> categoriesIds = ServicesUtils.getChildCategoriesList(datesDto.getLongValue());

        return adminPanelRepository.getCvrToOrdersBetweenDatesInCategoriesIds(datesDto.getMin(), datesDto.getMax(), categoriesIds, orderStateId, pageable);
    }

    @Override
    public Page<Tuple> getConversionFromViewToOrderInCategoryCriteria(DatesRangeAndValRequestDto datesDto, int pageNum, int dataOnPage,
                                                                      OrdersStatisticsSortEnum sortEnum, GeneralSortEnum sortType) {

        if(datesDto == null || !datesDto.isCorrect())
            throw new ApiException("Переданный DTO с диапазоном дат некорректен или дата min > max!");

        if (pageNum > 0)
            pageNum -= 1;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);

        Root<Order> mainQueryRoot = query.from(Order.class);
        Join<Order, OrderAndProductVariant> opvJoin = mainQueryRoot.join("orderAndPVList");
        Join<OrderAndProductVariant, ProductVariant> pvJoin = opvJoin.join("productVariant");

        // Подзапрос для выборки кол-ва посещений за день
        Subquery<Long> dailyVisitsSubquery = query.subquery(Long.class);
        Root<DailyVisits> dvRoot = dailyVisitsSubquery.from(DailyVisits.class);

        // Создать из date посещения диапазон datetime
        Expression<Timestamp> minDatetime = cb.function(
                "timestamp", Timestamp.class,
                cb.concat(dvRoot.get("date"), cb.literal(" 00:00:00")));

        Expression<Timestamp> maxDatetime = cb.function(
                "timestamp", Timestamp.class,
                cb.concat(dvRoot.get("date"), cb.literal(" 23:59:59")));

        Expression<Date> orderDateExpression = cb.function(
                "DATE", Date.class, mainQueryRoot.get("orderDate"));
        orderDateExpression.alias("order_date_alias");

        dailyVisitsSubquery.where(cb.equal(dvRoot.get("date"), orderDateExpression));
        //dailyVisitsSubquery.where(cb.equal(dvRoot.get("date"), mainQueryRoot.get("orderDate")));
        //dailyVisitsSubquery.where(cb.between(mainQueryRoot.get("orderDate"), minDatetime, maxDatetime));
        dailyVisitsSubquery.select(cb.sum(dvRoot.get("countVisits"))).groupBy(dvRoot.get("date"));


        // Основной запрос
        //Predicate predicate = cb.between(mainQueryRoot.get("orderDate"), datesDto.getMin(), datesDto.getMax());
        Predicate predicate = cb.between(orderDateExpression, datesDto.getMin(), datesDto.getMax());

        if (datesDto.getLongValue() != null && datesDto.getLongValue() != 0) {

            List<Long> childCategoriesIds = Services.categoriesService.getChildCategoriesIds(datesDto.getLongValue());

            predicate = childCategoriesIds != null ? cb.and(
                    predicate,
                    pvJoin.get("product").get("category").get("id").in(childCategoriesIds)
            ) : cb.ge(mainQueryRoot.get("id"), 0);
        }

        // Получить статус заказа
        Integer orderStateId = datesDto.getAdditionalValues() != null ? (int) datesDto.getAdditionalValues().get("state_id") : null;

        if (orderStateId != null && orderStateId > 0)
            predicate = cb.and(
                    predicate,
                   cb.equal(mainQueryRoot.get("orderState").get("id"), orderStateId)
            );

        // Отдельно определить выражения выборки для сортировки
        Expression<Number> visitsCountExpression = cb.coalesce(dailyVisitsSubquery.getSelection(), 0);
        Expression<?> cvrCountExpression = cb.quot(cb.toFloat(cb.countDistinct(mainQueryRoot.get("id"))), cb.toFloat(visitsCountExpression/*cb.coalesce(dailyVisitsSubquery.getSelection(), 0)*/));

        query.where(predicate);
        query.multiselect(
                orderDateExpression,
                //mainQueryRoot.get("orderDate"),
                cb.countDistinct(mainQueryRoot.get("id")),
                cb.coalesce(dailyVisitsSubquery.getSelection(), 0),
                cb.quot(cb.toFloat(cb.countDistinct(mainQueryRoot.get("id"))), cb.toFloat(cb.coalesce(dailyVisitsSubquery.getSelection(), 1))),
                cb.sum(mainQueryRoot.get("sum"))
        ).groupBy(mainQueryRoot.get("orderDate"))
           .having(
                   cb.greaterThanOrEqualTo(
                           dailyVisitsSubquery.getSelection(),
                           cb.countDistinct(mainQueryRoot.get("id"))
                   ));

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);

        typedQuery.setFirstResult(pageNum*dataOnPage);
        typedQuery.setMaxResults(dataOnPage);

        List<Tuple> rawResult = typedQuery.getResultList();
        int elementsCount = typedQuery.getResultList().size();

        return new PageImpl<>(rawResult, PageRequest.of(pageNum, dataOnPage), elementsCount);
    }

    @Override
    public Page<Object[]> getConversionFromViewToOrderForProduct(DatesRangeAndValRequestDto datesDto, int pageNum, int dataOnPage,
                                                                 OrdersStatisticsSortEnum sortEnum, GeneralSortEnum sortType) {

        if(datesDto == null || !datesDto.isCorrect())
            throw new ApiException("Переданный DTO с диапазоном дат некорректен или дата min > max!");

        if (pageNum > 0)
            pageNum -= 1;

        Pageable pageable = PageRequest.of(pageNum, dataOnPage, SortingUtils.createSortForOrdersCvrSelections(sortEnum, sortType));

        // Получить статус заказа
        int orderStateId = datesDto.getAdditionalValues() != null ? (int) datesDto.getAdditionalValues().get("state_id") : 0;

        return adminPanelRepository.getCvrToOrdersBetweenDatesForProduct(datesDto.getMin(), datesDto.getMax(), datesDto.getLongValue(), orderStateId, pageable);
    }

    @Override
    public Page<Object[]> getConversionFromViewToBasket(DatesRangeAndValRequestDto datesDto, int pageNum, int dataOnPage,
                                                        BasketsStatisticsSortEnum sortEnum, GeneralSortEnum sortType) {
        if(datesDto == null || !datesDto.isCorrect())
            throw new ApiException("Переданный DTO с диапазоном дат некорректен или дата min > max!");

        if (pageNum > 0)
            pageNum -= 1;

        Pageable pageable = PageRequest.of(pageNum, dataOnPage, SortingUtils.createSortForBasketsCvrSelections(sortEnum, sortType));

        return adminPanelRepository.getCvrToBasketsBetweenDatesForProduct(datesDto.getMin(), datesDto.getMax(), datesDto.getLongValue(), pageable);
    }

    @Override
    public Object[] getCvrValuesForOrdersInCategory(DatesRangeAndValRequestDto datesDto) {

        if(datesDto == null || !datesDto.isCorrect())
            throw new ApiException("Переданный DTO с диапазоном дат некорректен или дата min > max!");

        // Получить статус заказа
        int orderStateId = datesDto.getAdditionalValues() != null ? (int) datesDto.getAdditionalValues().get("state_id") : 0;

        // Если категория не задана, тогда будут выбраны все записи
        if (datesDto.getLongValue() == 0)
            return adminPanelRepository.getQuantityValuesForOrdersBetweenDatesInCategory(datesDto.getMin(), datesDto.getMax(),
                    datesDto.getLongValue(), orderStateId)[0];

        List<Long> categoriesIds = ServicesUtils.getChildCategoriesList(datesDto.getLongValue());

        return adminPanelRepository.getQuantityValuesForOrdersBetweenDatesInCategories(datesDto.getMin(), datesDto.getMax(), categoriesIds, orderStateId)[0];
    }

    @Override
    public Object[] getCvrValuesForOrdersForProduct(DatesRangeAndValRequestDto datesDto) {

        if(datesDto == null || !datesDto.isCorrect())
            throw new ApiException("Переданный DTO с диапазоном дат некорректен или дата min > max!");

        // Получить статус заказа
        int orderStateId = datesDto.getAdditionalValues() != null ? (int) datesDto.getAdditionalValues().get("state_id") : 0;

        return adminPanelRepository.getQuantityValuesForOrdersBetweenDatesForProduct(datesDto.getMin(), datesDto.getMax(), datesDto.getLongValue(), orderStateId)[0];
    }

    @Override
    public Page<Object[]> getProductsViewsFrequency(int pageNum, int dataOnPage,
                                                    ViewsFrequencySortEnum sortEnum, GeneralSortEnum sortType) {

        if (pageNum > 0)
            pageNum -= 1;

        Pageable pageable = PageRequest.of(pageNum, dataOnPage, SortingUtils.createSortForViewsFrequencySelection(sortEnum, sortType));

        return adminPanelRepository.getProdViewsFrequencyInCategories(pageable);
        //return new PageImpl<>(adminPanelRepository.getProdViewsFrequencyInCategories(), pageable, 0);
    }

    @Override
    public Page<Object[]> getCategoriesViewsFrequency(int pageNum, int dataOnPage,
                                                      ViewsFrequencySortEnum sortEnum, GeneralSortEnum sortType) {

        if (pageNum > 0)
            pageNum -= 1;

        Pageable pageable = PageRequest.of(pageNum, dataOnPage, SortingUtils.createSortForViewsFrequencySelection(sortEnum, sortType));

        return adminPanelRepository.getCategoriesViewsFrequency(pageable);
    }

    // Создать criteria query для постраничной выборки заказов
    private CriteriaQuery<Tuple> createQueryForProductsOrPvOrders(OrdersAndBasketsCountFiltersRequestDto filtersDto, ProductsOrVariantsEnum operationsEnum,
                                                                  ProductsOrVariantsCountSortEnum sortEnum, GeneralSortEnum sortType){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);

        Root<OrderAndProductVariant> opvRoot = query.from(OrderAndProductVariant.class);

        Join<OrderAndProductVariant, ProductVariant> pvJoin = opvRoot.join("productVariant");
        Join<OrderAndProductVariant, Order> orderJoin = opvRoot.join("order");
        Join<ProductVariant, Product> productJoin = pvJoin.join("product");

        List<Predicate> predicates = ServicesUtils.collectOrdersPredicates(cb, filtersDto, productJoin, orderJoin, opvRoot/*pvJoin*/);

        if (!predicates.isEmpty())
            query.where(predicates.toArray(new Predicate[0]));

        boolean isProductsSelection = operationsEnum == ProductsOrVariantsEnum.PRODUCTS;
        Expression<Long> countExpression = isProductsSelection ? cb.countDistinct(orderJoin.get("id")) : cb.count(orderJoin.get("id"));

        if (isProductsSelection) {
            query.multiselect(
                            productJoin.get("id"),
                            productJoin.get("name"),
                            countExpression
                    ).groupBy(productJoin.get("id"), productJoin.get("name"));
            if (sortEnum != null && sortType != null)
                SortingUtils.createSortQueryForProductsOrdersCount(cb, query, productJoin, countExpression, sortEnum, sortType);
        }
        else {
            query.multiselect(
                            productJoin.get("id"),
                            productJoin.get("name"),
                            pvJoin.get("title"),
                            pvJoin.get("id"),
                            countExpression
                    ).groupBy(pvJoin.get("id"));

            if (sortEnum != null && sortType != null) {
                From<?, ?> from = sortEnum == ProductsOrVariantsCountSortEnum.PRODUCT_ID || sortEnum == ProductsOrVariantsCountSortEnum.NAME ?
                        productJoin : pvJoin;
                SortingUtils.createSortQueryForVariantsOrdersCount(cb, query, from, countExpression, sortEnum, sortType);
            }

        }

        return query;
    }

    // Количество заказов каждого товара в категории + фильтр
    @Override
    public Page<Tuple> getOrdersCountForEachProduct(OrdersAndBasketsCountFiltersRequestDto filtersDto, int pageNum, int dataOnPage, ProductsOrVariantsEnum statisticsEnum,
                                                    ProductsOrVariantsCountSortEnum sortEnum, GeneralSortEnum sortType) {

        if (pageNum > 0)
            pageNum -= 1;

        // Сформировать запрос либо для выборки статистики по товарам, либо по вариантам
        CriteriaQuery<Tuple> query = createQueryForProductsOrPvOrders(filtersDto, statisticsEnum, sortEnum, sortType);

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);

        // Предварительный запрос для подсчёта общего кол-ва элементов
        int elementsCount = typedQuery.getResultList().size();

        typedQuery.setFirstResult(pageNum*dataOnPage);
        typedQuery.setMaxResults(dataOnPage);

        List<Tuple> rawResult = typedQuery.getResultList();

        return new PageImpl<>(rawResult, PageRequest.of(pageNum, dataOnPage), elementsCount);
    }

    @Override
    public List<Tuple> getOrdersCountForEachProduct(OrdersAndBasketsCountFiltersRequestDto filtersDto, ProductsOrVariantsEnum statisticsEnum) {
        // Сформировать запрос либо для выборки статистики по товарам, либо по вариантам
        CriteriaQuery<Tuple> query = createQueryForProductsOrPvOrders(filtersDto, statisticsEnum, null, null);

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);


        return typedQuery.getResultList();
    }


    // Создать criteria query для выборки вариантов товаров с кол-вом добавлений в корзину близким к максимальному
    private CriteriaQuery<Tuple> createQueryForTopProductsInBasket(OrdersAndBasketsCountFiltersRequestDto filtersDto, Float percentage,
                                                                   ProductsOrVariantsCountSortEnum sortEnum, GeneralSortEnum sortType){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Основной запрос
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);

        Root<BasketAndProductVariant> bpvRoot = query.from(BasketAndProductVariant.class);
        Path<Basket> basketPath = bpvRoot.get("basket");

        Join<OrderAndProductVariant, ProductVariant> pvJoin = bpvRoot.join("productVariant");
        Join<ProductVariant, Product> productJoin = pvJoin.join("product");

        // Запрос подсчёта максимального элемента
        CriteriaQuery<Long> countMaxQuery = cb.createQuery(Long.class);
        Root<BasketAndProductVariant> countQueryRoot = countMaxQuery.from(BasketAndProductVariant.class);

        // Соединения с таблицами для дополнительного запроса
        Join<BasketAndProductVariant, ProductVariant> countMaxQueryPvJoin = countQueryRoot.join("productVariant");
        Path<Basket>  countMaxQuerybasketPath = countQueryRoot.get("basket");
        Path<Product> countMaxQueryproductPath = countMaxQueryPvJoin.get("product");

        List<Predicate> predicatesMainQuery = ServicesUtils.collectBasketsPredicates(cb, filtersDto, productJoin, basketPath, pvJoin);

        // Отдельные предикаты - обязательно, иначе падение из-за использования одного и того же root для разных запросов
        List<Predicate> predicatesSubQuery = ServicesUtils.collectBasketsPredicates(cb, filtersDto, countMaxQueryproductPath, countMaxQuerybasketPath, countMaxQueryPvJoin);

        if (!predicatesSubQuery.isEmpty())
            countMaxQuery.where(predicatesSubQuery.toArray(new Predicate[0]));

        if (!predicatesMainQuery.isEmpty())
            query.where(predicatesMainQuery.toArray(new Predicate[0]));

        // Подсчёт именно фактов добавлений в корзину без подсчёта кол-ва вариантов в каждой корзине
        countMaxQuery.select(cb.count(countMaxQueryPvJoin.get("id")))
                .groupBy(countMaxQueryPvJoin.get("id"));

        List<Long> countsList = entityManager.createQuery(countMaxQuery).getResultList();
        long maxCount = Collections.max(countsList);

        // Расчёт значения близкого к максимальному
        maxCount = Math.round(maxCount*(1-percentage));

        Expression<Long> countExpression = cb.count(pvJoin.get("id"));

        // Сформировать основной запрос
        query.multiselect(
                        productJoin.get("id"),
                        productJoin.get("name"),
                        pvJoin.get("title"),
                        pvJoin.get("id"),
                        countExpression
                ).groupBy(pvJoin.get("id"))
                .having(cb.ge(countExpression, maxCount));


        if (sortEnum != null && sortType != null) {
            // Если задана сортировка по id или имени товара, тогда задаём соответствующий объект соединения таблиц
            From<?, ?> from = sortEnum == ProductsOrVariantsCountSortEnum.PRODUCT_ID || sortEnum == ProductsOrVariantsCountSortEnum.NAME ?
                    productJoin : pvJoin;
            SortingUtils.createSortQueryForVariantsOrdersCount(cb, query, from, countExpression, sortEnum, sortType);
        }

        return query;
    }

    @Override
    public Page<Tuple> getTopProductsInBasket(OrdersAndBasketsCountFiltersRequestDto filtersDto, int pageNum, int dataOnPage, float percentage,
                                              ProductsOrVariantsCountSortEnum sortEnum, GeneralSortEnum sortType) {
        if (pageNum > 0)
            pageNum -= 1;

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(createQueryForTopProductsInBasket(filtersDto, percentage, sortEnum, sortType));

        // Предварительный запрос для подсчёта общего кол-ва элементов
        // TODO: переделать на нормальный подсчёт отдельным запросом
        int elementsCount = typedQuery.getResultList().size();

        typedQuery.setFirstResult(pageNum*dataOnPage);
        typedQuery.setMaxResults(dataOnPage);

        List<Tuple> rawResult = typedQuery.getResultList();

        return new PageImpl<>(rawResult, PageRequest.of(pageNum, dataOnPage), elementsCount);
    }

    @Override
    public List<Tuple> getTopProductsInBasket(OrdersAndBasketsCountFiltersRequestDto filtersDto, float percentage) {
        TypedQuery<Tuple> typedQuery = entityManager.createQuery(createQueryForTopProductsInBasket(filtersDto, percentage, null, null));

        return typedQuery.getResultList();
    }

    // Выборка товаров просмотренных определённым покупателем
    /*@Override
    public Page<ProductViews> getProductsViewsForCustomer(CustomerRequestDto customerDto, int pageNum, int dataOnPage) {

        if (customerDto == null || customerDto.getFingerPrint() == null && customerDto.getId() == null && customerDto.getEmail() == null)
            throw new ApiException("Найти товары просмотренные покупателем не удалось. Dto задан некорректно!");

        if (pageNum > 0)
            pageNum -= 1;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Запрос идёт от таблицы просмотров товаров
        CriteriaQuery<ProductViews> query = cb.createQuery(ProductViews.class);
        Root<ProductViews> root = query.from(ProductViews.class);
        Path<Visitor> visitorPath = root.get("visitor");
        Path<Customer> customerPath = visitorPath.get("customers");

        // Сформировать предикаты
        // Todo: убрать отсюда всю работу с fingerprint'ом посетителя и оставить только проверку по email/id покупателя (Visitor имеет коллекцию customers в любом случае)
        Predicate predicate;
        if (customerDto.getId() != null && customerDto.getFingerPrint() != null && customerDto.getEmail() != null)
            predicate = cb.or(
                cb.equal(visitorPath.get("fingerprint"), customerDto.getFingerPrint()),
                cb.equal(customerPath.get("id"), customerDto.getId()),
                cb.equal(customerPath.get("email"), customerDto.getEmail())
            );
        else if (customerDto.getId() == null && customerDto.getEmail() == null)
            predicate = cb.equal(visitorPath.get("fingerprint"), customerDto.getFingerPrint());
        else if(customerDto.getEmail() != null)
            predicate = cb.equal(customerPath.get("email"), customerDto.getEmail());
        else
            predicate = cb.equal(customerPath.get("id"), customerDto.getId());

        query.where(predicate);

        TypedQuery<ProductViews> typedQuery = entityManager.createQuery(query);

        long elementCount = ServicesUtils.countCustomerProductsViews(entityManager, customerDto);

        // Для пагинации
        typedQuery.setFirstResult(pageNum*dataOnPage);
        typedQuery.setMaxResults(dataOnPage);

        List<ProductViews> viewsList = typedQuery.getResultList();

        return new PageImpl<>(viewsList, PageRequest.of(pageNum, dataOnPage), elementCount);
    }*/

    // Выборка товаров просмотренных определённым покупателем
    @Override
    public Page<ProductViews> getProductsViewsForCustomer(CustomerStatRequestDto customerDto, int pageNum, int dataOnPage) {

        if (customerDto == null || customerDto.getId() == null && customerDto.getEmail() == null)
            throw new ApiException("Найти товары просмотренные покупателем не удалось. Dto задан некорректно!");

        if (pageNum > 0)
            pageNum -= 1;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Запрос идёт от таблицы просмотров товаров
        CriteriaQuery<ProductViews> query = cb.createQuery(ProductViews.class);
        Root<ProductViews> root = query.from(ProductViews.class);
        Path<Visitor> visitorPath = root.get("visitor");
        Path<Customer> customerPath = visitorPath.get("customers");

        // Сформировать предикаты
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

        TypedQuery<ProductViews> typedQuery = entityManager.createQuery(query);

        long elementCount = PaginationUtils.countCustomerProductsViews(entityManager, customerDto);

        // Для пагинации
        typedQuery.setFirstResult(pageNum*dataOnPage);
        typedQuery.setMaxResults(dataOnPage);

        List<ProductViews> viewsList = typedQuery.getResultList();

        return new PageImpl<>(viewsList, PageRequest.of(pageNum, dataOnPage), elementCount);
    }

}
