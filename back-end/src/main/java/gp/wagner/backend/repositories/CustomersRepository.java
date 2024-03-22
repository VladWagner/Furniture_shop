package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entites.orders.Customer;
import gp.wagner.backend.domain.entites.users.User;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomersRepository extends JpaRepository<Customer,Long> {

    //Добавление записи о пользователе
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert into customers
            (surname, 'name', patronymic, phone_number, email)
        values
            (:surname, :name, :patronymic, :email, :phone_number)
    """)
    void insertCustomer(@Param("surname") String surname, @Param("name") String name,@Param("patronymic") String patronymic,
                     @Param("email") String email, @Param("phone_number") int phone);

    //Изменение пользователя
    @Transactional
    @Query(nativeQuery = true,
    value = """
    update customers set
        'name' = :name,
        surname = :surname,
        patronymic = :patronymic,
        email = :email,
        phone_number = :phone_number
    where id = :id
    """)
    void updateCustomer(@Param("id") long id, @Param("surname") String surname, @Param("name") String name, @Param("patronymic") String patronymic,
                        @Param("email") String email, @Param("phone_number") int phone);


    //Получить maxId
    @Query(value = """
    select
        max(c.id)
    from
        Customer c
    """)
    long getMaxId();

    //Выборка пользователей по email
    Optional<Customer> getCustomerByEmail(String email);

    Optional<Customer> getCustomerByPhoneNumber(int phoneNumber);

    // Выборка пограничных значений фильтров
    @Query(nativeQuery = true, value = """
    with customers_preselection as (
        select
            c.created_at,
            count(orders.id) as orders_count,
            sum(orders.general_products_amount) as ordered_units_count,
            avg(orders.sum/orders.general_products_amount) as avg_order_price,
            sum(orders.sum) as orders_sum
        from customers c join orders on c.id = orders.customer_id
        group by c.id
    )
    
    select
        MIN(cps.created_at),
        MAX(cps.created_at),
    
        MIN(cps.orders_count),
        MAX(cps.orders_count),
    
        MIN(cps.ordered_units_count),
        MAX(cps.ordered_units_count),
    
        MIN(cps.avg_order_price),
        MAX(cps.avg_order_price),
    
        MIN(cps.orders_sum),
        MAX(cps.orders_sum)
    from customers_preselection cps
    
    """)
    Tuple getFilterValues();

    // Выборка пограничных значений фильтров для конкретных покупателей
    @Query(nativeQuery = true, value = """
    with customers_preselection as (
        select
            c.created_at,
            count(orders.id) as orders_count,
            sum(orders.general_products_amount) as ordered_units_count,
            avg(orders.sum/orders.general_products_amount) as avg_order_price,
            sum(orders.sum) as orders_sum
        from customers c join orders on c.id = orders.customer_id
        where c.id in :ids
        group by c.id
    )
    
    select
        MIN(cps.created_at),
        MAX(cps.created_at),
    
        MIN(cps.orders_count),
        MAX(cps.orders_count),
    
        MIN(cps.ordered_units_count),
        MAX(cps.ordered_units_count),
    
        MIN(cps.avg_order_price),
        MAX(cps.avg_order_price),
    
        MIN(cps.orders_sum),
        MAX(cps.orders_sum)
    from customers_preselection cps
    
    """)
    Tuple getFilterValues(@Param("ids") List<Long> customersIds);

}
