package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.dto.request.crud.OrderRequestDto;
import gp.wagner.backend.domain.dto.request.filters.OrderReportDto;
import gp.wagner.backend.domain.entities.orders.Order;
import gp.wagner.backend.domain.entities.orders.OrderAndProductVariant;
import gp.wagner.backend.domain.entities.orders.PaymentMethod;
import gp.wagner.backend.domain.entities.products.ProductVariant;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.orders.OrdersSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.orders.OrdersStatisticsSortEnum;
import jakarta.mail.MessagingException;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;


public interface OrdersService {


    //Выборка всех записей
    Page<Order> getAll(int pageNum, int dataOnPage, OrdersSortEnum sortEnum, GeneralSortEnum sortType);

    // Добавление записи. Возвращает: id созданного заказа в таблице + его код
    SimpleTuple<Long, Long> create(Order order);

    SimpleTuple<Long, Long> create(int orderStateId, int customerId);

    SimpleTuple<Long, Long> create(OrderRequestDto dto, String fingerprint, String ip) throws MessagingException;
    PaymentMethod createPaymentMethod(String methodName);

    // Задать заказываемые варианты товаров. Т.е. что именно будет заказывать пользователь
    void insertProductVariants(OrderRequestDto orderDto);

    //Изменение записи
    void update(Order order);
    void update(long id ,int orderStateId, int customerId, Long orderCode, int sum);

    // Изменить сумму при изменении стоимости варианта товара (только для заказов в статусе 1, т.е. начальном, поскольку все остальные нужны для ретроспективы)
    void updateOrdersOnPvPriceChanged(ProductVariant changedPv);

    // Изменить сумму при скрытии варианта товара (только для заказов в статусе 1, т.е. начальном, поскольку все остальные нужны для ретроспективы)
    void updateOrdersOnPvHidden(ProductVariant pv, List<ProductVariant> changedPvList);

    // Удалить вариант товара и изменить сумму, если товар был удалён (только для заказов в статусе 1 - в ожидании и 2 - связались)
    void updateOrdersOnPvDelete(ProductVariant pv, List<ProductVariant> deletedPvList);

    // Изменение заказов при восстановлении товаров из скрытия
    void updateOrdersOnPvDisclosure(ProductVariant pv, List<ProductVariant> disclosedPvList);

    // Изменить статус заказа
    void updateStatus(long orderCode, int orderStateId);

    // Отменить заказ
    void cancelOrder(Long orderCode) throws MessagingException;

    //Выборка записи под id
    Order getById(Long id);

    //Получить статистику заказов по дням за определённый период
    Page<Tuple> getDailyOrdersStatistics(OrderReportDto reportDto, int pageNum, int dataOnPage,
                                         OrdersStatisticsSortEnum sortEnum, GeneralSortEnum sortType);

    // Выборка заказа по его коду
    Order getByOrderCode(long code);

    //Выборка записи по коду заказа
    List<Order> getByOrdersByCodes(List<Long> ordersCodes);

    // Получить все заказы по email покупателя
    Page<Order> getOrdersByCustomerEmail(String email, Long id, int pageNum, int dataOnPage, OrdersSortEnum sortEnum, GeneralSortEnum sortType);

    // Получить все заказы пользователя, если такие существуют
    Page<Order> getOrdersByUser(Long userId, int pageNum, int dataOnPage, OrdersSortEnum sortEnum, GeneralSortEnum sortType);

    //Получить заказы для определённого варианта товара по id
    Page<OrderAndProductVariant> getOrdersByProductVariant(long pvId, int pageNum, int dataOnPage);

    //Получить заказы для определённого ТОВАРА по его id
    Page<Order> getOrdersByProductId(long productId, int pageNum, int dataOnPage, OrdersSortEnum sortEnum, GeneralSortEnum sortType);

    //Получение максимального id - последнее добавленное значение
    long getMaxId();

    // Получить список способов оплаты
    List<PaymentMethod> getPaymentMethods();

    //Удаление заказа
    long deleteOrder(Long id, Long code);

    //Удалить вариант товара из заказа по коду заказа
    boolean deletePVFromOrder(long code, long productVariantId);

    //Получить все заказы по email покупателя
    <T> List<T> getOrdersOrOpvByOrderIdOrCode(Long id, Long orderCode, Class<T> type);

    // Получить пограничные значения дат для заказов с определённым статусом/в определённой категории
    SimpleTuple<Date, Date> getOrdersDatesBorders(Long statusId, Integer categoryId);

    // Пересчитать суммы заказов для определённых вариантов
    void recountSumsForVariants(Long pvId, List<Long> pvIdsList);


}
