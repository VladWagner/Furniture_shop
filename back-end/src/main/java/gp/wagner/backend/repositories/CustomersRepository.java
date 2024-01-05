package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entites.orders.Customer;
import gp.wagner.backend.domain.entites.users.User;
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
}
