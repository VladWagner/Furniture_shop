package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.crud.user.PasswordResetRequestDto;
import gp.wagner.backend.domain.dto.request.crud.user.PasswordUpdateRequestDto;
import gp.wagner.backend.domain.dto.request.crud.user.UserRequestDto;
import gp.wagner.backend.domain.dto.request.filters.UsersFilterRequestDto;
import gp.wagner.backend.domain.dto.response.filters.UserFilterValuesDto;
import gp.wagner.backend.domain.entites.tokens.PasswordResetToken;
import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.domain.entites.users.UserPassword;
import gp.wagner.backend.domain.entites.users.UserRole;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.domain.exception.suppliers.UserNotFound;
import gp.wagner.backend.domain.exception.suppliers.UserRoleNotFound;
import gp.wagner.backend.domain.specifications.UsersSpecifications;
import gp.wagner.backend.infrastructure.ServicesUtils;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.PasswordResetTokenRepository;
import gp.wagner.backend.repositories.UsersRepository;
import gp.wagner.backend.repositories.UsersRolesRepository;
import gp.wagner.backend.services.interfaces.UsersService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UsersService {

    @PersistenceContext
    private EntityManager entityManager;

    //Репозиторий пользователей
    private UsersRepository usersRepository;

    @Autowired
    public void setUsersRepository(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    //Репозиторий ролей
    private UsersRolesRepository usersRolesRepository;

    @Autowired
    public void setUsersRepository(UsersRolesRepository rolesRepository) {
        this.usersRolesRepository = rolesRepository;
    }

    // Репозиторий токенов восстановления пароля
    private PasswordResetTokenRepository prtRepository;

    @Autowired
    public void setUsersRepository(PasswordResetTokenRepository repository) {
        this.prtRepository = repository;
    }

    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // Подтверждение почты пользователя - пока что просто mock-метод
    @Override
    public void confirmEmail(long userId) {
        User userToConfirm = usersRepository.findById(userId).orElseThrow(() -> new ApiException(
                String.format("Пользователь с id: %d не найден. Подтвердить пользователя не удалось!", userId)
        ));

        userToConfirm.setIsConfirmed(true);

        usersRepository.saveAndFlush(userToConfirm);
    }

    @Override
    public long create(User user) {

        if (user == null)
            throw new ApiException("Объект User равен null, создать пользователя не удалось!");

        return usersRepository.save(user).getId();
    }

    @Override
    public User create(UserRequestDto userDto) {

        if (userDto == null || userDto.getPassword() == null)
            throw new ApiException("Создать пользователя не удалось. Dto задан некорректно!");

        // Проверка на уникальность логина и пароля

        // При регистрации пользователю будет присвоена базовая роль, которую впоследствии сможет поменять админ
        String encryptedPassword = passwordEncoder.encode(userDto.getPassword());

        User newUser = new User(null, userDto.getLogin(),userDto.getName(), userDto.getEmail(), usersRolesRepository.getBasicRole());

        // Задать пароль для созданного пользователя
        UserPassword userPassword = new UserPassword(null, encryptedPassword, newUser);

        newUser.setUserPassword(userPassword);

        return usersRepository.saveAndFlush(newUser);
    }

    // Сменить пароль пользователя
    @Override
    public void updatePassword(PasswordUpdateRequestDto passwordUpdateDto) {
        User user = usersRepository
                .findById(passwordUpdateDto.getUserId())
                .orElseThrow(() -> new ApiException(String.format("Не удалось найти пользователя с id: %d", passwordUpdateDto.getUserId())));

        String password = user.getUserPassword().getPassword();

        if (!passwordEncoder.matches(passwordUpdateDto.getOldPassword(), password))
            throw new SecurityException(String.format("Старый пароль для пользователя %s задан неверно!", user.getUserLogin()));

        password = passwordEncoder.encode(passwordUpdateDto.getNewPassword());

        user.getUserPassword().setPassword(password);

        usersRepository.save(user);

    }

    @Override
    public void update(long userId, String login, String email, long roleId) {

        if (usersRepository.findById(userId).isEmpty() || usersRolesRepository.findById(roleId).isEmpty())
            throw new ApiException("Пользователя или роли пользователя не существует с заданным id");

        if ((login == null || login.isEmpty()) || (email == null || email.isEmpty()))
            throw new ApiException(String.format("Переданные данные неполные для обновления пользователя с id: %d", userId));

        usersRepository.updateUser(userId, login, email, roleId);
    }

    @Override
    public SimpleTuple<User, Boolean> update(UserRequestDto userDto) {

        if (userDto == null || userDto.getId() == null)
            throw new ApiException("В методе редактирования пользователя DTO задано некорректно!");

        User oldUser = usersRepository.findById(userDto.getId())
                .orElseThrow(
                        () -> new ApiException(
                                String.format("Пользователь с id: %d не найден. Изменить запись пользователя не удалось!", userDto.getId())
                        )
                );

        /*if (!oldUser.getIsConfirmed())
            throw new ApiException("Email аккаунта не подтверждён! Подтвердите почту, чтобы вносить изменения.");*/

        String oldName = oldUser.getName();
        String oldLogin = oldUser.getUserLogin();

        oldUser.setName(userDto.getName());
        oldUser.setUserLogin(userDto.getLogin());

        if (!oldUser.getEmail().equals(userDto.getEmail()) && userDto.getEmail() != null) {
            oldUser.setEmail(userDto.getEmail());
            oldUser.setIsConfirmed(false);

            // В будущем отправка запроса на подтверждение
        }

        // Если заданное имя и логин отличаются предыдущего, тогда нужно будет сгенерировать новое изображение (если оно не загружено пользователем)
        boolean generateNewPhoto = (userDto.getName() != null && oldName != null && !oldName.equals(userDto.getName())) || !oldLogin.equals(userDto.getLogin());

        return new SimpleTuple<>(usersRepository.saveAndFlush(oldUser), generateNewPhoto);
    }

    @Override
    public void changeRole(long userId, long roleId) {

        User user = usersRepository.findById(userId)
                .orElseThrow(new UserNotFound(userId));

        UserRole role = usersRolesRepository.findById(roleId).orElseThrow(new UserRoleNotFound(roleId));

        if (user.getUserRole().getRole().equals(role.getRole()))
            return;

        user.setUserRole(role);
        update(user);
    }

    @Override
    public Page<User> getByKeyword(String keyword, UsersFilterRequestDto filter, int pageNum, int limit) {

        if (pageNum > 0)
            pageNum -= 1;


        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = new ArrayList<>();

        keyword = '%'+keyword+'%';
        // Условие выборки по ключевым словам
        predicates.add(cb.or(
                cb.like(root.get("name"), keyword),
                cb.like(root.get("userLogin"), keyword),
                cb.like(root.get("email"), keyword)
        ));

        // Спецификации для фильтрации по остальным полям. На данном этапе не зависят от CriteriaQuery и Root
        Specification<User> specification = UsersSpecifications.createGeneralUsersFilterSpecification(filter);

        if (specification != null)
            predicates.add(specification.toPredicate(root, query, cb));

        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(limit);
        typedQuery.setFirstResult(pageNum*limit);

        List<User> users = typedQuery.getResultList();

        // Общее кол-во элементов с таким же ключевым словом и входящее в теже фильтра
        long elements = ServicesUtils.countUsersByKeyword(keyword, entityManager, specification);

        return new PageImpl<>(users, PageRequest.of(pageNum, limit), elements);
    }

    @Override
    public Page<User> getAll(int pageNum, int limit) {

        if (pageNum > 0)
            pageNum -= 1;

        return usersRepository.findAll(PageRequest.of(pageNum, limit));
    }

    @Override
    public void update(User user) {
        if (user == null)
            throw new ApiException("Не получилось изменить запись о пользователе. Задано некорректное значение!");

        usersRepository.saveAndFlush(user);

    }


    @Override
    public User getById(Long id) {
        return usersRepository.findById(id).orElse(null);
    }

    @Override
    public User getByEmail(String email) {
        return usersRepository.getUserByEmail(email).orElse(null);
    }

    @Override
    public long getMaxId() {
        return 0;
    }

    @Override
    public UserFilterValuesDto getFilterValues() {

        Object[] datesBorders = usersRepository.getRegistrationDatesRange()[0];

        Date minDate = (Date) datesBorders[0];
        Date maxDate = (Date) datesBorders[1];

        List<UserRole> roles = usersRepository.getPossibleRoles();

        if ((minDate == null && maxDate == null) || roles == null || roles.isEmpty())
            throw new ApiException("Полностью выбрать пограничные значения не удалось!");

        return new UserFilterValuesDto(minDate, maxDate, roles);
    }

    @Override
    public boolean userWithLoginExists(String login) {

        if (login == null)
            throw new ApiException("Значение логина задано некорректно!");

        return usersRepository.existsUsersByUserLogin(login);
    }

    @Override
    public boolean userWithEmailExists(String email) {

        if (email == null)
            throw new ApiException("Значение логина задано некорректно!");
        return usersRepository.existsUsersByEmailIs(email);
    }

    @Override
    public void createTokenForPasswordRecovery(String mail) throws MessagingException {
        User user = usersRepository.getUserByEmail(mail).orElseThrow(new UserNotFound(mail, null));

        // Сформировать токен
        String token = UUID.randomUUID().toString();

        // Проверить, существует ли токен для пользователя и он не просрочен
        PasswordResetToken prt = prtRepository.findByUserId(user.getId()).orElse(null);

        // Если для пользователя нет заданного токена или его срок действия истёк
        if (prt == null || prt.isExpired()){

            // Удалить токен с истёкшим сроком действия
            if (prt != null)
                prtRepository.delete(prt);

            // Сохранить новый токен
            prt = prtRepository.saveAndFlush(new PasswordResetToken(token, user));
        }

        // Отправить сообщение - тестовая почта. В проде убрать и оставить user.
        String emailForTest = "saabnakyul@yandex.ru";

        // Services.emailService.sendPasswordResetTokenSimple(emailForTest, prt.getToken(), user.getUserLogin());
        Services.emailService.sendPasswordResetTokenMime(emailForTest, prt.getToken(), user.getUserLogin());
    }

    @Override
    public User savePasswordAfterReset(PasswordResetRequestDto resetDto) {

        Date nowDate = new Date();

        // Сначала удалить все просроченные токены
        prtRepository.deleteExpiredTokens(nowDate/*new Date()*/);

        String token = resetDto.getToken();
        PasswordResetToken prt = prtRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("Срок действия токена для восстановления пароля истёк, либо данный пользователь не сбрасывал пароль!"));

        User user = prt.getUser();

        String encryptedPassword = passwordEncoder.encode(resetDto.getNewPassword());

        // Задать пароль для созданного пользователя (нужно получить текущую запись пароля для пользователя)
        UserPassword userPassword = user.getUserPassword();
        userPassword.setPassword(encryptedPassword);

        user.setUserPassword(userPassword);

        // Задать дату обновления пользователя (обновили же пароль)
        user.setUpdatedAt(nowDate.toInstant());

        // Удалить использованный токен
        prtRepository.delete(prt);

        return usersRepository.saveAndFlush(user);
    }

}
