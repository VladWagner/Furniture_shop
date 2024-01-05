package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.dto.request.crud.OrderRequestDto;
import gp.wagner.backend.domain.dto.request.filters.OrderReportDto;
import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.entites.orders.OrderAndProductVariant;
import gp.wagner.backend.infrastructure.SimpleTuple;
import org.springframework.data.domain.Page;

import java.util.List;


public interface OrdersService {


    //Выборка всех записей
    Page<Order> getAll(int pageNum, int dataOnPage);

    // Добавление записи. Возвращает: id созданного заказа в таблице + его код
    SimpleTuple<Long, Long> create(Order order);

    SimpleTuple<Long, Long> create(int orderStateId, int customerId);

    SimpleTuple<Long, Long> create(OrderRequestDto dto);

    // Задать заказываемые варианты товаров. Т.е. что именно будет заказывать пользователь
    void insertProductVariants(long orderId, OrderRequestDto orderDto);

    //Изменение записи
    void update(Order order);
    void update(long id ,int orderStateId, int customerId, Long orderCode, int sum);


    //Выборка записи под id
    Order getById(Long id);

    //Получить статистику заказов по дням за определённый период
    SimpleTuple<String, Integer> getDailyOrdersStatistics(OrderReportDto reportDto);

    // Выборка заказа по его коду
    Order getByOrderCode(long code);

    //Выборка записи по коду заказа
    List<Order> getByOrdersByCodes(List<Long> ordersCodes);

    //Получить все корзины по email покупателя
    List<Order> getOrdersByEmail(String email);

    //Получить заказы для определённого варианта товара по id
    List<OrderAndProductVariant> getOrdersByProductVariant(long pvId);

    //Получить заказы для определённого ТОВАРА по его id
    List<OrderAndProductVariant> getOrdersAndPvByProduct(long productId);

    //Получение максимального id - последнее добавленное значение
    long getMaxId();

    //Удаление заказа
    long deleteOrder(Long id, Long code);

}
