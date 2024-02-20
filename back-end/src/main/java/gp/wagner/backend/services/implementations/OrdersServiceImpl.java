package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.crud.OrderRequestDto;
import gp.wagner.backend.domain.dto.request.filters.OrderReportDto;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.orders.Customer;
import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.entites.orders.OrderAndProductVariant;
import gp.wagner.backend.domain.entites.orders.OrderState;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.*;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.orders.OrdersSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.orders.OrdersStatisticsSortEnum;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.CustomersRepository;
import gp.wagner.backend.repositories.orders.OrdersAndProductVariantsRepository;
import gp.wagner.backend.repositories.orders.OrdersRepository;
import gp.wagner.backend.repositories.products.ProductVariantsRepository;
import gp.wagner.backend.services.interfaces.OrdersService;
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

@Service
public class OrdersServiceImpl implements OrdersService {

    //region Инжекция бинов
    // Репозиторий
    private OrdersRepository ordersRepository;

    @Autowired
    public void setOrdersRepository(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    // Репозиторий покупателей
    private CustomersRepository customersRepository;

    @Autowired
    public void setCustomersRepository(CustomersRepository repository) {
        this.customersRepository = repository;
    }

    // Репозиторий таблицы заказываемых вариантов товаров
    private OrdersAndProductVariantsRepository opvRepository;

    @Autowired
    public void setOrdersAndProductVariantsRepository(OrdersAndProductVariantsRepository repository) {
        this.opvRepository = repository;
    }

    // Репозиторий таблицы заказываемых вариантов товаров
    private ProductVariantsRepository productVariantsRepository;

    @Autowired
    public void setProductVariantsRepository(ProductVariantsRepository repository) {
        this.productVariantsRepository = repository;
    }

    @PersistenceContext
    private EntityManager entityManager;
    //endregion

    @Override
    public SimpleTuple<Long, Long> create(Order order) {

        if (order == null)
            return new SimpleTuple<>(-1L, -1L);


        order.setCode(Utils.generateOrderCode(order.getCustomer().getId()));
        long createdOrderId = ordersRepository.saveAndFlush(order).getId();

        return new SimpleTuple<>(createdOrderId, order.getCode());
    }

    @Override
    public SimpleTuple<Long, Long> create(int orderStateId, int customerId) {

        long orderCode = Utils.generateOrderCode(customerId);
        ordersRepository.insertOrder(orderStateId, customerId, orderCode);

        return new SimpleTuple<>(ordersRepository.getMaxId(), orderCode);
    }

    // Добавление заказа
    @Override
    public SimpleTuple<Long, Long> create(OrderRequestDto dto) {

        if (dto == null || dto.getCustomer() == null)
            return null;

        // Добавить покупателя, если он не задан
        Long customerId = dto.getCustomer().getId();

        // Проверить наличие созданного посетителя с таким отпечатком браузера
        Visitor visitor = null;
        if (!dto.getCustomer().getFingerPrint().isEmpty()){
            visitor = Services.visitorsService.saveIfNotExists(dto.getCustomer().getFingerPrint());
        }

        if (customerId == null || customerId <= 0)
            customerId = customersRepository.save(new Customer(dto.getCustomer(), visitor)).getId();
        else{
            // Получить запись о переданном покупателе и объекте из БД с тем же id
            Customer newCustomer = new Customer(dto.getCustomer(), visitor);
            Customer oldCustomer = customersRepository.findById(customerId).orElse(null);

            //Сравнить предыдущую запись с заданной
            if (!newCustomer.isEqualTo(oldCustomer)) {

                // Если покупатель изменён, но при этом в старой записи существует Visitor
                if (newCustomer.getVisitor() == null && oldCustomer != null && oldCustomer.getVisitor() != null)
                    newCustomer.setVisitor(oldCustomer.getVisitor());

                //Если значения полей !=, тогда пересохранить запись, поскольку произошло редактирование
                customersRepository.save(newCustomer);
            }

        }

        long orderCode = Utils.generateOrderCode(customerId);

        // Добавить информацию о самом заказе
        ordersRepository.insertOrder(dto.getStateId(), customerId.intValue(), orderCode);

        Order createdOrder = ordersRepository.findOrderByCode(orderCode).orElse(null);

        if (createdOrder == null)
            return new SimpleTuple<>(-1L, -1L);

        // Добавить список товаров
        List<OrderAndProductVariant> opvList = new LinkedList<>();

        ProductVariant productVariant;

        int orderSum = 0;
        int productsAmount = 0;

        for (Map.Entry<Integer,Integer> entry : dto.getProductVariantIdAndCount().entrySet()) {

            productVariant = productVariantsRepository.findById(entry.getKey().longValue()).orElse(null);

            if (productVariant == null)
                continue;

            opvList.add(new OrderAndProductVariant(null, entry.getValue(), productVariant, createdOrder));

            orderSum += productVariant.getPrice();
            productsAmount += entry.getValue();

        }

        // Установить сумму заказа и общее кол-во единиц вариантов товара
        createdOrder.setSum(orderSum);
        createdOrder.setGeneralProductsAmount(productsAmount);

        update(createdOrder);

        opvRepository.saveAll(opvList);

        //TODO: реализовать асинхронную отправку уведомления || синхронное добавление в таблицу уведомлений

        return new SimpleTuple<>(createdOrder.getId(), createdOrder.getCode()) ;
    }

    // Добавить/Изменить товары в заказе
    @Override
    public void insertProductVariants(OrderRequestDto orderDto) {

        if (orderDto == null)
            throw new ApiException("Dto заказа задан некорректно!");

        //Order foundOrder = ordersRepository.findOrderByIdOrCode(orderDto.getId(), orderDto.getCode());
        Order foundOrder = getOrdersOrOpvByOrderIdOrCode(orderDto.getId(), orderDto.getCode(), Order.class).get(0);

        if (foundOrder == null)
            throw new ApiException(String.format("Не удалось найти заказ id: %d и кодом: %d", orderDto.getId(), orderDto.getCode()));

        List<OrderAndProductVariant> opvList = addOrUpdateProductVariants(orderDto.getProductVariantIdAndCount().entrySet(), foundOrder);

        opvRepository.saveAll(opvList);

        // Максимально корректно пересчитать сумму заказа - пока что дольше, но зато точно правильно
        opvList = getOrdersOrOpvByOrderIdOrCode(foundOrder.getId(), foundOrder.getCode(), OrderAndProductVariant.class);

        // Пересчитать сумму заказа + проводить фильтрацию, если заказ активен и уже не является историей
        ServicesUtils.countSumInOrder(foundOrder, opvList, foundOrder.getOrderState().getId() == Constants.MutableOrderStateId);

        update(foundOrder);

    }

    // Добавление вариантов товаров в заказ, либо их редактирование
    public List<OrderAndProductVariant> addOrUpdateProductVariants(Set<Map.Entry<Integer, Integer>> pvAndCountEntry, Order order){

        if (order.getId() == null && order.getCode() <= 0)
            throw new ApiException("Заказ не существует!");

        // Сформировать список товаров в заказе + подсчитать общую сумму заказа
        List<OrderAndProductVariant> newOpvList = new LinkedList<>();

        // Получить список вариантов товаров для определённого заказа
        List<OrderAndProductVariant> oldOpvList = getOrdersOrOpvByOrderIdOrCode(order.getId(), order.getCode(), OrderAndProductVariant.class);

        ProductVariant productVariant;
        OrderAndProductVariant oldOpv;

        for (Map.Entry<Integer,Integer> entry : pvAndCountEntry) {

            productVariant = productVariantsRepository.findById(entry.getKey().longValue()).orElse(null);

            if (productVariant == null)
                continue;

            // Найти существующую запись таблицы OrderAndProductVariant
            ProductVariant finalProductVariant = productVariant;
            oldOpv = oldOpvList.stream()
                    .filter(opv -> opv.getProductVariant().getId().equals(finalProductVariant.getId()))
                    .findFirst()
                    .orElse(null);

            // Сохранить || добавить вариант товара
            if (oldOpv != null){
                oldOpv.setProductsAmount(entry.getValue());
                newOpvList.add(oldOpv);
            }
            else {
                newOpvList.add(new OrderAndProductVariant(null, entry.getValue(), productVariant, order));
            }

        }

        return newOpvList;

    }

    @Override
    public void update(Order order) {
        ordersRepository.saveAndFlush(order);
    }

    @Override
    public void update(long id, int orderStateId, int customerId, Long orderCode, int sum) {

        if (id <= 0 || orderStateId <= 0 || customerId <= 0 || orderCode <= 0 || sum <= 0)
            throw new ApiException("Incorrect arguments in order.update");

        ordersRepository.updateOrder(id, orderStateId, customerId, orderCode, sum);

    }

    @Override
    public void updateOrdersOnPvPriceChanged(ProductVariant changedPv) {

        // Найти заказы с заданным вариантом товара
        List<Order> ordersToChange = findOrdersByPvIdAndStateId(changedPv.getId(), null, Constants.MutableOrderStateId);

        if (ordersToChange.isEmpty())
            return;

        // Найти все записи в таблице многие ко многим
        List<OrderAndProductVariant> opvAll = opvRepository.findOrdersAndPvByIdListOrCodesList(ordersToChange.stream().map(Order::getId).toList());

        // Пересчитать сумму для каждого заказа с учётом изменённого варианта товара

        ServicesUtils.countSumInOrders(ordersToChange, opvAll);

        // Сохранить заказы
        ordersRepository.saveAll(ordersToChange);

    }

    // Скрытие варианта товара - пересчёт суммы в корзине
    @Override
    public void updateOrdersOnPvHidden(ProductVariant pv, List<ProductVariant> changedPvList) {

        if (pv == null && changedPvList == null)
            return;

        List<Long> changedPvIdsList = changedPvList != null ? changedPvList.stream().map(ProductVariant::getId).toList() : null;
        Long singlePvId = pv != null ? pv.getId() : null;

        // Найти заказы с заданным вариантом/вариантами товара
        List<Order> ordersToChange = findOrdersByPvIdAndStateId(singlePvId, changedPvIdsList, Constants.MutableOrderStateId);

        if (ordersToChange.isEmpty())
            return;

        // Найти все записи в таблице многие ко многим
        List<OrderAndProductVariant> opvAll = opvRepository.findOrdersAndPvByIdListOrCodesList(ordersToChange.stream().map(Order::getId).toList());

        // Для расчёта оставить только записи, где варианты не скрыты и не удалены
        opvAll = opvAll.stream()
                .filter(e -> e.getProductVariant().getShowVariant() &&
                        (e.getProductVariant().getIsDeleted() == null || !e.getProductVariant().getIsDeleted())
                ).toList();

        // Задаём список
        ServicesUtils.countSumInOrders(ordersToChange, opvAll);

        ordersRepository.saveAll(ordersToChange);

    }

    @Override
    public void updateOrdersOnPvDelete(ProductVariant pv, List<ProductVariant> deletedPvList) {

        if (pv == null && deletedPvList == null)
            return;

        List<Long> changedPvIdsList = deletedPvList != null ? deletedPvList.stream().map(ProductVariant::getId).toList() : null;
        Long singlePvId = pv != null ? pv.getId() : null;

        // Найти заказы с заданным удаляемыми вариантом/вариантами товара
        List<Order> ordersToChange = findOrdersByPvIdAndStateId(singlePvId, changedPvIdsList, Constants.MutableOrderStateId);

        if (ordersToChange.isEmpty())
            return;

        List<OrderAndProductVariant> opvAll = opvRepository.findOrdersAndPvByIdListOrCodesList(ordersToChange.stream().map(Order::getId).toList());

        // Получить элементы, которые нужно удалить в наихудшем случае сложность скорее всего может быть O(n^2).
        // Возможно стоит сделать changedPvIdsList в виде Map<Long, Integer> и сохранять переданный id + флаг
        List<OrderAndProductVariant> opvToDelete = opvAll.stream()
                .filter(e -> singlePvId != null ? e.getProductVariant().getId().equals(singlePvId) :
                        changedPvIdsList.stream().anyMatch(pvId -> pvId.equals(e.getProductVariant().getId()))
                )
                .toList();

        // Для расчёта оставить только записи, где варианты не удалены и не скрыты
        opvAll = opvAll.stream()
                .filter(e -> e.getProductVariant().getShowVariant() &&
                        (e.getProductVariant().getIsDeleted() == null || !e.getProductVariant().getIsDeleted())
                ).toList();

        ServicesUtils.countSumInOrders(ordersToChange, opvAll);

        // Изменить заказы
        ordersRepository.saveAll(ordersToChange);

        // Удалить необходимые OrderAndProductVariants
        opvRepository.deleteAll(opvToDelete);

    }

    // Изменение заказов при восстановлении товаров из скрытия
    @Override
    public void updateOrdersOnPvDisclosure(ProductVariant pv, List<ProductVariant> disclosedPvList) {

        // Противоположное от скрытия действие
        updateOrdersOnPvHidden(pv, disclosedPvList);
    }

    // Изменить статус заказа
    @Override
    public void updateStatus(long orderCode, int orderStateId) {
        if(getByOrderCode(orderCode) == null)
            throw new ApiException(String.format("Заказ с кодом %d не существует!", orderCode));

        ordersRepository.updateOrderState(null, orderCode, orderStateId);
    }

    @Override
    public Page<Order> getAll(int pageNum, int dataOnPage, OrdersSortEnum sortEnum, GeneralSortEnum sortType) {

        return ordersRepository.findAll(PageRequest.of(pageNum-1, dataOnPage, SortingUtils.createSortForOrders(sortEnum, sortType)));
    }

    @Override
    public Order getById(Long id) {
        return ordersRepository.findById(id).orElse(null);
    }

    // Дневная статистика по товарам
    @Override
    public Page<Tuple> getDailyOrdersStatistics(OrderReportDto reportDto, int pageNum, int dataOnPage
            , OrdersStatisticsSortEnum sortEnum, GeneralSortEnum sortType) {

        if(reportDto == null || reportDto.getMinDate() == null || !reportDto.getMinDate().before(reportDto.getMaxDate()))
            throw new ApiException("Переданный DTO для отчёта по товарам задан некорректно!");

        if (pageNum > 0)
            pageNum -= 1;

        return ordersRepository.getDailyOrdersStatistics(reportDto.getMinDate(), reportDto.getMaxDate(), reportDto.getStateId(),
                PageRequest.of(pageNum, dataOnPage, SortingUtils.createSortForOrdersStatistics(sortEnum, sortType)));
    }

    @Override
    public Order getByOrderCode(long code) {
        return ordersRepository.findOrderByCode(code).orElse(null);
    }

    @Override
    public List<Order> getByOrdersByCodes(List<Long> ordersCodes) {
        return ordersRepository.getOrdersByCodes(ordersCodes);
    }

    @Override
    public List<Order> getOrdersByEmail(String email, OrdersSortEnum sortEnum, GeneralSortEnum sortType) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Для формирования запросов
        CriteriaQuery<Order> query = cb.createQuery(Order.class);

        // Составная таблица заказов
        Root<Order> orderRoot = query.from(Order.class);

        // Присоединить таблицу покупателей
        Join<Order, Customer> orderCustomerJoin = orderRoot.join("customer");

        // Предикат для запроса
        Predicate predicate = cb.equal(orderCustomerJoin.get("email"), email);

        query.where(predicate);

        SortingUtils.createSortQueryForOrders(cb, query, orderRoot, sortEnum, sortType);

        return entityManager.createQuery(query).getResultList();
    }

    // Получение всех заказов для определённого варианта товара
    @Override
    public Page<OrderAndProductVariant> getOrdersByProductVariant(long pvId, int pageNum, int dataOnPage) {

        if (pageNum > 0)
            pageNum -= 1;

        return opvRepository.findOrderAndProductVariantsByProductVariantId(pvId, PageRequest.of(pageNum, dataOnPage));
    }

    // Получение всех заказов для определённого товара
    @Override
    public Page<Order> getOrdersByProductId(long productId, int pageNum, int dataOnPage, OrdersSortEnum sortEnum, GeneralSortEnum sortType) {

        if (pageNum > 0)
            pageNum -= 1;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Для формирования запросов
        CriteriaQuery<Order> query = cb.createQuery(Order.class);

        // Таблица со заказываемыми товарами
        Root<OrderAndProductVariant> root = query.from(OrderAndProductVariant.class);

        // Присоединение сущности productVariant
        Join<OrderAndProductVariant, ProductVariant> pvJoin = root.join("productVariant");
        Path<Product> product = pvJoin.get("product");
        Join<OrderAndProductVariant, Order> orderJoin = root.join("order");

        query.where(cb.equal(product.get("id"), productId));
        query.select(orderJoin);

        SortingUtils.createSortQueryForOrders(cb, query, orderJoin, sortEnum, sortType);

        TypedQuery<Order> typedQuery = entityManager.createQuery(query);

        typedQuery.setFirstResult(pageNum*dataOnPage);
        typedQuery.setMaxResults(dataOnPage);

        List<Order> opvList = typedQuery.getResultList();

        long elementsCount =  ServicesUtils.countProductsOrVariantsOrders(entityManager, productId, Product.class);

        return new PageImpl<>(opvList, PageRequest.of(pageNum, dataOnPage), elementsCount);
    }

    @Override
    public long getMaxId() {
        return ordersRepository.getMaxId();
    }

    @Override
    public long deleteOrder(Long id, Long code) {

        //Order deletingOrder = ordersRepository.findOrderByIdOrCode(id, code);
        Order deletingOrder = getOrdersOrOpvByOrderIdOrCode(id, code, Order.class).get(0);

        if (deletingOrder != null){

            // Удалить записи из таблицы М к М
            opvRepository.deleteAll(deletingOrder.getOrderAndPVList());

            // Удалить запись заказа
            ordersRepository.delete(deletingOrder);
            return deletingOrder.getId();
        }

        return 0;
    }

    @Override
    public boolean deletePVFromOrder(long code, long productVariantId) {

        // Удалять можно только из необработанного заказа - всё остальное для ретроспективы

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Для формирования запросов
        CriteriaQuery<OrderAndProductVariant> query = cb.createQuery(OrderAndProductVariant.class);

        // Таблица со заказываемыми товарами
        Root<OrderAndProductVariant> root = query.from(OrderAndProductVariant.class);

        Join<OrderAndProductVariant, Order> orderJoin = root.join("order");

        // Получить путь к атрибуту заказа - статус заказа + вариант товара
        Path<OrderState> orderStatePath = orderJoin.get("orderState");
        Path<ProductVariant> productVariantPath = root.get("productVariant");

        // Предикат для нахождения удаляемого элемента
        Predicate predicate = cb.and(
                cb.equal(orderJoin.get("code"), code),
                cb.equal(productVariantPath.get("id"), productVariantId),
                cb.equal(orderStatePath.get("id"), Constants.MutableOrderStateId)
        );

        query.where(predicate);

        OrderAndProductVariant opv;

        try {
            opv = entityManager.createQuery(query).getSingleResult();
        } catch (Exception e) {
            opv = null;
        }

        // Если запись найдена, тогда удаляем
        if (opv == null)
            return false;

        opvRepository.delete(opv);

        // Найти заказ для уменьшения суммы
        Order editedOrder = getByOrderCode(code);

        List<OrderAndProductVariant> opvList = getOrdersOrOpvByOrderIdOrCode(editedOrder.getId(), null, OrderAndProductVariant.class);

        ServicesUtils.countSumInOrder(editedOrder, opvList, true);

        update(editedOrder);

        return true;
    }

    // Частично generic метод для получения заказа по id или вариантов товаров под конкретный заказ
    @Override
    public <T> List<T> getOrdersOrOpvByOrderIdOrCode(Long id, Long orderCode, Class<T> type) {

        boolean isOpvType = type == OrderAndProductVariant.class;
        boolean isOrderType = type == Order.class;

        // Если задан некорректные параметры или несоответствующий тип
        if (id == null && orderCode == null || (!isOpvType && !isOrderType))
            return null;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Для формирования запросов
        CriteriaQuery<T> query = cb.createQuery(type);

        // Таблица со заказываемыми товарами
        Root<T> root = query.from(type);

        // Присоединить заказы, если производим выборку из М к М таблицы
        Path<T> orderPath = null;

        if (isOpvType)
            orderPath = root.get("order");

        Predicate selectionPredicate;

        // Сформировать условия в зависимости от параметров и типа
        if (id == null)
            selectionPredicate = cb.equal(isOrderType ? root.get("code") : orderPath.get("code"), orderCode);
        else if (orderCode == null)
            selectionPredicate = cb.equal(isOrderType ? root.get("id") : orderPath.get("id"), id);
        else
            selectionPredicate = cb.and(
                    cb.equal(isOrderType ? root.get("id") : orderPath.get("id"), id),
                    cb.equal(isOrderType ? root.get("code") : orderPath.get("code"), orderCode)
            );

        query.where(selectionPredicate);

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public SimpleTuple<Date, Date> getOrdersDatesBorders(Long statusId, Integer categoryId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);

        Root<Order> root = query.from(Order.class);
        Path<OrderState> orderStatePath = root.get("orderState");
        Path<OrderAndProductVariant> opvPath = root.get("orderAndPVList");

        Path<ProductVariant> productVariantPath = opvPath.get("productVariant");
        Path<Product> productPath = productVariantPath.get("product");
        Path<Category> categoryPath = productPath.get("category");

        Expression<Date> minDate = cb.min(root.get("orderDate")).as(Date.class);
        Expression<Date> maxDate = cb.max(root.get("orderDate")).as(Date.class);

        Predicate predicate;

        if ((statusId != null && categoryId != null) && (statusId != 0 && categoryId != 0))
            predicate = cb.and(
                    cb.equal(orderStatePath.get("id"), statusId),
                    cb.equal(categoryPath.get("id"), categoryId)
            );
        else
            predicate = statusId != null && statusId != 0 ? cb.equal(orderStatePath.get("id"), statusId) :
                        categoryId != null && categoryId != 0 ? cb.equal(categoryPath.get("id"), categoryId) : null;

        if (predicate != null)
            query.where(predicate);

        query.multiselect(minDate, maxDate);

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);

        Tuple rawTuple = typedQuery.getSingleResult();


        return new SimpleTuple<>(rawTuple.get(0, Date.class), rawTuple.get(1, Date.class));
    }

    // Выборка товаров и определённым вариантом товара и статусом
    private List<Order> findOrdersByPvIdAndStateId(Long pvId, List<Long> pvIdList, long statusId){

        if (pvId == null && pvIdList == null)
            return null;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Для формирования запросов
        CriteriaQuery<Order> query = cb.createQuery(Order.class);

        // Таблица с заказываемыми товарами
        Root<Order> root = query.from(Order.class);

        // Присоединение сущности productVariant
        Join<Order, OrderAndProductVariant> opvJoin = root.join("orderAndPVList");

        // Таблица вариантов товаров
        Path<ProductVariant> productVariantPath = opvJoin.get("productVariant");

        // Присоединить таблицу состояний заказов
        Path<OrderState> orderStatePath = root.get("orderState");

        // Предикат выборки только по id или по списку значений
        Predicate predicate = pvId != null ? cb.and(
                cb.equal(productVariantPath.get("id"), pvId),
                cb.equal(orderStatePath.get("id"), statusId)
        ) : cb.equal(orderStatePath.get("id"), statusId);

        // Поиск по списку id
        if (pvIdList != null && pvId == null){
            CriteriaBuilder.In<Long> cbIn = cb.in(productVariantPath.get("id"));

            pvIdList.forEach(cbIn::value);

            predicate = cb.and(
                    predicate,
                    cbIn
            );

        }

        query.where(predicate);

        return entityManager.createQuery(query).getResultList();
    }
}