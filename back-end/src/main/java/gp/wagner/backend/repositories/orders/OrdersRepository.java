package gp.wagner.backend.repositories.orders;

import gp.wagner.backend.domain.entites.orders.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    @Query(nativeQuery = true,
    value = """
    update orders set
              order_state_id = :order_state
    where
        if(:id is null or :id <= 0, (:code is not null and :code > 0 and :code = code), id = :id)
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

    //Получить maxId
    @Query(value = """
    select
        max(o.id)
    from
        Order o
    """)
    long getMaxId();


}
