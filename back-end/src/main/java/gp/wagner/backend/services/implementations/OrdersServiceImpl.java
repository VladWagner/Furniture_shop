package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.crud.OrderRequestDto;
import gp.wagner.backend.domain.dto.request.filters.OrderReportDto;
import gp.wagner.backend.domain.entites.orders.Customer;
import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.entites.orders.OrderAndProductVariant;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.repositories.CustomersRepository;
import gp.wagner.backend.repositories.OrdersAndProductVariantsRepository;
import gp.wagner.backend.repositories.OrdersRepository;
import gp.wagner.backend.repositories.ProductVariantsRepository;
import gp.wagner.backend.services.interfaces.OrdersService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    public Page<Order> getAll(int pageNum, int dataOnPage) {
        return ordersRepository.findAll(PageRequest.of(pageNum-1, dataOnPage));
    }

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
        if (customerId == null || customerId <= 0)
            customerId = customersRepository.save(new Customer(dto.getCustomer())).getId();

        else{
            // Получить запись о переданном покупателе и объекте из БД с тем же i
            Customer newCustomer = new Customer(dto.getCustomer());
            Customer oldCustomer = customersRepository.findById(customerId).orElse(null);

            //Сравнить запись с заданной
            if (!newCustomer.isEqualTo(oldCustomer))
                //Если значения полей !=, тогда пересохранить запись, поскольку произошло редактирование
                customersRepository.save(newCustomer);

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

        for (Map.Entry<Integer,Integer> entry : dto.getProductVariantIdAndCount().entrySet()) {

            productVariant = productVariantsRepository.findById(entry.getKey().longValue()).orElse(null);

            if (productVariant == null)
                continue;

            opvList.add(new OrderAndProductVariant(null, entry.getValue(), productVariant, createdOrder));

            orderSum += productVariant.getPrice();

        }

        // Установить сумму заказа
        createdOrder.setSum(orderSum);

        update(createdOrder);

        opvRepository.saveAll(opvList);

        //TODO: реализовать асинхронную отправку уведомления || синхронное добавление в таблицу уведомлений

        return new SimpleTuple<>(createdOrder.getId(), createdOrder.getCode()) ;
    }

    @Override
    public void insertProductVariants(long orderId, OrderRequestDto orderDto) {

        Order foundOrder = ordersRepository.findById(orderId).orElse(null);

        if (orderDto == null || foundOrder == null)
            return;

        List<OrderAndProductVariant> opvList = new LinkedList<>();

        ProductVariant productVariant;

        for (Map.Entry<Integer,Integer> entry : orderDto.getProductVariantIdAndCount().entrySet()) {

            productVariant = productVariantsRepository.findById(entry.getKey().longValue()).orElse(null);

            if (productVariant != null)
                opvList.add(new OrderAndProductVariant(null, entry.getValue(), productVariant, foundOrder));

        }

        opvRepository.saveAll(opvList);

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
    public Order getById(Long id) {
        return ordersRepository.findById(id).orElse(null);
    }

    // Дневная статистика
    @Override
    public SimpleTuple<String, Integer> getDailyOrdersStatistics(OrderReportDto reportDto) {
        return null;
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
    public List<Order> getOrdersByEmail(String email) {

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

        return entityManager.createQuery(query).getResultList();
    }

    // Получение всех заказов для определённого варианта товара
    @Override
    public List<OrderAndProductVariant> getOrdersByProductVariant(long pvId) {

        return opvRepository.findOrderAndProductVariantsByProductVariantId(pvId);
    }

    // Получение всех заказов для определённого товара
    @Override
    public List<OrderAndProductVariant> getOrdersAndPvByProduct(long productId) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Для формирования запросов
        CriteriaQuery<OrderAndProductVariant> query = cb.createQuery(OrderAndProductVariant.class);

        // Таблица со заказываемыми товарами
        Root<OrderAndProductVariant> root = query.from(OrderAndProductVariant.class);

        // Присоединение сущности productVariant
        Join<OrderAndProductVariant, ProductVariant> pvJoin = root.join("productVariant");

        // Получить товар из общего агрегата OrderAndProductVariant + ProductVariant
        Path<Product> product = pvJoin.get("product");

        query.where(cb.equal(product.get("id"), productId));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public long getMaxId() {
        return ordersRepository.getMaxId();
    }

    @Override
    public long deleteOrder(Long id, Long code) {

        Order deletingOrder = ordersRepository.findOrderByIdOrCode(id, code);

        if (deletingOrder != null){
            ordersRepository.delete(deletingOrder);
            return deletingOrder.getId();
        }

        return 0;
    }
}
