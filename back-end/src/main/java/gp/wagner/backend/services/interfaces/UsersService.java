package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.domain.entites.users.UserPassword;

import java.util.List;


public interface UsersService {

    //Добавление записи
    long create(User visitor);
    long create(String ipAddress, String fingerPrint);

    //Изменение записи
    void update(User user, UserPassword userPassword);
    void update(long userId, String login, String email, int roleId, String password);

    //Выборка всех записей
    public List<User> getAll();

    //Выборка записи под id
    User getById(Long id);

    //Выборка записи по email
    User getByEmail(String email);

    //Получение максимального id
    long getMaxId();

}
