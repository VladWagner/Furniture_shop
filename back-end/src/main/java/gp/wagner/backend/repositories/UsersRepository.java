package gp.wagner.backend.repositories;

import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.domain.entites.users.UserRole;
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

    // Добавление записи о пользователе
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert into users
            (login, email, role_id)
        values
            (:login, :email, :role);
        
    """)
    int registerUser(@Param("login") String login, @Param("email") String email,
                     @Param("role") int roleId);

    // Добавление пароля пользователя
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
    value = """
        insert into users_passwords
            (user_id, password)
        values
            (:user_id, :password);
        
    """)
    int insertUserPassword(@Param("user_id") long userId, @Param("password") String password);

    // Изменение пользователя
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
                    @Param("role") long roleId);


    // Получить maxId
    @Query(value = """
    select
        max(u.id)
    from
        User u
    """)
    long getMaxId();

    // Получить диапазон дат регистрации пользователей для фильтрации
    @Query(nativeQuery = true, value = """

    select
        MIN(u.created_at) as minDate,
        MAX(u.created_at) as maxDate
    from
    users u join user_roles ur on u.role_id = ur.id
          join users_passwords up on u.id = up.user_id;
    
    """)
    Object[][] getRegistrationDatesRange();

    // Получить роли заданные у всех пользователей для фильтрации
    @Query(value = """
    select
        u.userRole
    from
        User u
    group by u.userRole
    
    """)
    List<UserRole> getPossibleRoles();

    // Найти пользователя по логину или email
    @Query(value = """
    select
        u
    from
        User u
    where
        ((:email is not null and :login is null) and u.email = :email) or
        ((:login is not null and :email is null) and u.email = :email) or
        ((:email is not null and :login is not null) and (u.email = :email and u.userLogin = :login))
    """)
    Optional<User> getUserByEmailOrLogin(@Param("email") String email, @Param("login") String login);

    // Проверка существования записей с заданным логином
    boolean existsUsersByUserLogin(String login);

    // Проверка существования записей с заданным email
    boolean existsUsersByEmailIs(String email);

    //Выборка пользователей по email
    Optional<User> getUserByEmail(String email);

    // Выборка по имени
    Optional<User> getUsersByNameLike(String name);

    Optional<User> getUsersByUserLoginLike(String login);
    Optional<User> getUsersByUserLoginEquals(String login);
}
