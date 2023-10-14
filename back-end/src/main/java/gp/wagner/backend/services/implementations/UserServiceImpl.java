package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.domain.entites.users.UserPassword;
import gp.wagner.backend.repositories.UsersRepository;
import gp.wagner.backend.services.interfaces.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UsersService {

    //Репозиторий
    private UsersRepository repository;

    @Autowired
    public void setRepository(UsersRepository repository) {
        this.repository = repository;
    }

    @Override
    public long create(User visitor) {
        return 0;
    }

    @Override
    public long create(String ipAddress, String fingerPrint) {
        return 0;
    }

    @Override
    public void update(User user, UserPassword userPassword) {

    }

    @Override
    public void update(long userId, String login, String email, int roleId, String password) {

    }

    @Override
    public List<User> getAll() {
        return repository.findAll();
    }

    @Override
    public User getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public User getByEmail(String email) {
        return repository.getUserByEmail(email).orElse(null);
    }

    @Override
    public long getMaxId() {
        return 0;
    }
}
