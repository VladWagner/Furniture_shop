package gp.wagner.backend.security.services;


import gp.wagner.backend.domain.entities.users.User;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.security.models.UserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String nameOrEmail) throws UsernameNotFoundException {

        // Осуществить поиск по username или email
        boolean findByEmail = Utils.emailIsValid(nameOrEmail);

        User user = findByEmail ? Services.usersService.getByEmailNullable(nameOrEmail) : Services.usersService.getByLoginNullable(nameOrEmail)
                .orElse(null);

        if (user == null)
           throw new UsernameNotFoundException(String.format("Пользователь с %s %s не найден!", findByEmail ? "email" : "username", nameOrEmail));

        return new UserDetailsImpl(user);
    }
}
