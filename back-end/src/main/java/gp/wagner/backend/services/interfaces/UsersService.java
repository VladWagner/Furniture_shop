package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.dto.request.crud.user.PasswordResetRequestDto;
import gp.wagner.backend.domain.dto.request.crud.user.PasswordUpdateRequestDto;
import gp.wagner.backend.domain.dto.request.crud.user.UserRequestDto;
import gp.wagner.backend.domain.dto.request.filters.UsersFilterRequestDto;
import gp.wagner.backend.domain.dto.response.filters.UserFilterValuesDto;
import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.infrastructure.SimpleTuple;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;


public interface UsersService {


    // Выборка всех записей
    Page<User> getAll(int pageNum, int limit);

    //Выборка записи под id
    User getById(Long id);

    User confirmEmail(String token);

    // Добавление записи
    long create(User visitor);
    User create(UserRequestDto userDto) throws MessagingException;

    // Повторная отправка сообщения для
    User resendConfirmationMessage(String email) throws MessagingException;

    // Изменение записи
    void updatePassword(PasswordUpdateRequestDto passwordUpdateDto);
    void update(long userId, String login, String email, long roleId);
    void update(User user);
    SimpleTuple<User, Boolean> update(UserRequestDto userDto);

    void changeRole(long userId, long roleId);

    // Найти пользователей по имени
    Page<User> getByKeyword(String part, UsersFilterRequestDto filter, int pageNum, int limit);

    //Выборка записи по email
    User getByEmail(String email);

    //Получение максимального id
    long getMaxId();

    // Получить пограничные значения фильтрации
    UserFilterValuesDto getFilterValues();

    // Проверка существования пользователей с заданным Login'ом
    boolean userWithLoginExists(String login);

    // Проверка существования пользователей с заданным email
    boolean userWithEmailExists(String email);

    /** Запрос на восстановление пароля - здесь формируется токен и отправляется email
     * @param mail почта пользователя, которая задаётся на странице восстановления пароля и она должна быть задана у пользователя
     */
    void createTokenForPasswordRecovery(String mail) throws MessagingException;

    // Изменение пароля по токену, полученному из email и фронта
    User savePasswordAfterReset(PasswordResetRequestDto resetDto);

}
