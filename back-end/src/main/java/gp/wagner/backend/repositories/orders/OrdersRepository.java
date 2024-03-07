package gp.wagner.backend.repositories.orders;

import gp.wagner.backend.domain.entites.orders.Order;
import gp.wagner.backend.domain.entites.orders.OrderState;
import gp.wagner.backend.domain.entites.orders.PaymentMethod;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Order,Long> {

    //Добавление записи заказа
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert orders
        (order_state_id, customer_id, code)
        values
            (:order_state, :customer, :code)
    """)
    void insertOrder(@Param("order_state") int orderStateId, @Param("customer") int customerId, @Param("code") long orderCode);

    //Изменение заказа
    @Modifying
    @Transactional
    @Query(nativeQuery = true,
    value = """
    update orders set
              order_state_id = :order_state,
              customer_id = :customer,
              code = :code,
              'sum' = :sum
   where id = :id
    """)
    void updateOrder(@Param("id") long id, @Param("order_state") int orderStateId, @Param("customer") int customerId, @Param("code") long orderCode, @Param("sum") int orderSum);

    //Изменение состояния заказа по id записи в таблице/коду заказа
    @Modifying
    @Transactional
    @Query(nativeQuery = true,
    value = """
    update orders set
              order_state_id = :order_state
    where
        (:id is null or :id <= 0) and (:code is not null and :code > 0 and :code = code) or (:id is not null and :id > 0 and id = :id)
    """)
    void updateOrderState(@Param("id") Long id, @Param("code") Long orderCode, @Param("order_state") int orderStateId);

    // Получить заказ по коду или id
    @Query(value = """
    Select
        o
    from Order o
    where
            CASE
                WHEN (:id IS NULL OR :id <= 0) AND (:code IS NOT NULL AND :code > 0) THEN (o.code = :code )
                ELSE (o.id = :id)
            END
    """)
    Order findOrderByIdOrCode(@Param("id") Long id,@Param("code") Long orderCode);

    //Получить заказ по коду
    Optional<Order> findOrderByCode(long code);

    //Получить заказ по списку кодов
    @Query(value = """
    select
        order
    from
        Order order
    where order.code in :codes
""")
    List<Order> getOrdersByCodes(@Param("codes") List<Long> codes);

    //Получить заказы по email покупателя
    @Query(value = """
    select
        order
    from
        Order order
    where order.customer.email = :email
    """)
    List<Order> getOrdersByEmail(@Param("email") String email);

    // Статистика по заказам по дням
    @Query(nativeQuery = true, value = """
    with date_and_orders_count as (
        select
            DATE(o.order_date) as order_date_alias,
            coalesce((select sum(dv.count)
                      from daily_visits dv where dv.date = order_date_alias group by dv.date), 0) as visits,
            COUNT(DISTINCT(o.id)) as orders_amount,
            SUM(o.sum) as orders_sum
        from orders o
        where o.order_date between :date_lo and :date_hi and
                ((:state is not null and :state > 0 and o.order_state_id = :state)
                      or :state is null or :state <= 0)
        group by order_date_alias, visits),
       doc_with_cvr as (
        select
            *,
            doc.order_date_alias as order_date,
            coalesce(doc.orders_amount/visits, 0) as cvr
        from
            date_and_orders_count doc
        where
            doc.visits >= doc.orders_amount)
  
        select
            dwc.order_date,
            dwc.orders_amount,
            dwc.visits,
            dwc.cvr,
            dwc.orders_sum
        from
            doc_with_cvr dwc
    """)
    Page<Tuple> getDailyOrdersStatistics(@Param("date_lo") Date dateLo, @Param("date_hi") Date dateHi, @Param("state") Integer orderStateId, Pageable pageable);

    //Получить maxId
    @Query(value = """
    select
        max(o.id)
    from
        Order o
    """)
    long getMaxId();

    @Query(value = """
    select
        order.orderState
    from
        Order order
    where order.orderState.id = :order_state_id
    """)
    Optional<OrderState> getOrderStateById(@Param("order_state_id") int orderStateId);

    @Query(value = """
    select
        pm
    from
        PaymentMethod pm
    where pm.id = :payment_method_id
    """)
    Optional<PaymentMethod> getPaymentMethodById(@Param("payment_method_id") long paymentMethodId);

    @Query(value = """
    select
        pm
    from
        PaymentMethod pm
    order by pm.id asc
    """)
    List<PaymentMethod> getAllPaymentMethods();

    // Сохранить созданный способ оплаты
    @Modifying
    @Transactional
    @Query(nativeQuery = true,
            value = """
    insert payment_methods
        (method_name)
    values
        (:name)
    """)
    void insertPaymentMethod(@Param("name") String methodName);

}
