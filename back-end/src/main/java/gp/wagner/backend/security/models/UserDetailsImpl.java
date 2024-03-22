package gp.wagner.backend.security.models;

import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.infrastructure.Constants;
import gp.wagner.backend.infrastructure.enums.UsersRolesEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

    // Если происходи аутентификация через получение записи из БД
    private User user;

    // id пользователя (если сам пользователь задан не будет)
    private Long userId;

    // Роли пользователя
    private List<SimpleGrantedAuthority> simpleGrantedAuthorities;

    // Пароль
    private String userPassword;

    // Имя пользователя
    private String userLogin;

    // Флаг подтверждения
    private boolean isConfirmed;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    // Конструктор на случай, если работаем без обращения к БД и всё получаем из access токена (кроме пароля)
    public UserDetailsImpl(@NotNull List<SimpleGrantedAuthority> simpleGrantedAuthorities, long userId, String userLogin, boolean isConfirmed, String userPassword) {
        this.simpleGrantedAuthorities = simpleGrantedAuthorities;

        this.userId = userId;
        this.userPassword = userPassword;
        this.userLogin = userLogin;
        this.isConfirmed = isConfirmed;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        /*return user == null ? null :
                new ArrayList<>(
                        List.of(
                                new SimpleGrantedAuthority(UsersRolesEnum.getRoleEnum(user.getUserRole().getRole()).getRoleName())
                        )
                );*/

        // Если пользователь задан не был, значит информация о ролях была получена и токена
        if (user == null)
            return simpleGrantedAuthorities;

        List<SimpleGrantedAuthority> authorities = new ArrayList<>(
                List.of(
                        new SimpleGrantedAuthority(UsersRolesEnum.getRoleEnum(user.getUserRole().getRole()).getRoleName())
                )
        );

        // Если в список задана не базовая роль
        if (!user.getUserRole().getRole().equals(Constants.BASIC_USER_ROLE.getRole()))
            authorities.add(new SimpleGrantedAuthority(UsersRolesEnum.CUSTOMER.getRoleName()));

        return authorities;
    }

    @Override
    public String getPassword() {
        return user != null ? user.getUserPassword().getPassword() : this.userPassword;
    }

    @Override
    public String getUsername() {
        return user != null ? user.getUserLogin() : this.userLogin;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Флаг подтверждения почты
    @Override
    public boolean isEnabled() {
        return user != null ? user.getIsConfirmed() : this.isConfirmed;
    }

    public Long getUserId() {
        return userId;
    }
}