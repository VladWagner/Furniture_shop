package gp.wagner.backend.repositories;

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
public interface UsersRepository extends JpaRepository<User,Long> {

    //Добавление записи о пользователе
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert into users
            (login, email, role_id)
        values 
            (:login, :email, :role)
    """)
    int registerUser(@Param("login") String login, @Param("email") String email,
                     @Param("role") int roleId);

    //Изменение пользователя
    @Transactional
    @Query(nativeQuery = true,
    value = """
    update users set
        login = :login,
        email = :email,
        role_id = :role
    where id = :id
    """)
    void updateUser(@Param("id") long id, @Param("login") String login, @Param("email") String email,
                    @Param("role") int roleId);


    //Получить maxId
    @Query(value = """
    select
        max(u.id)
    from
        User u
    """)
    long getMaxId();

    //Выборка пользователей по email
    Optional<User> getUserByEmail(String email);
    List<User> getUsersByUserLogin(String login);
}
