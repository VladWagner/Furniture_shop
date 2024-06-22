package gp.wagner.backend.domain.exceptions.suppliers;

import gp.wagner.backend.domain.entities.tokens.PasswordResetToken;
import gp.wagner.backend.domain.entities.tokens.VerificationToken;
import gp.wagner.backend.domain.entities.users.User;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Supplier;

// Данный класс нужен, чтобы постоянно не писать сообщения в supplier в методах Optional<>.ElseThrow()
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TokenExpired implements Supplier<ApiException> {

    // Тип токена, у которого истёк срок
    private Class<?> tokenType;

    // Пользователь, для которого создавался токен
    private String userNameOrLogin;

    public TokenExpired(Class<?> tokenType) {
        this.tokenType = tokenType;
    }
    public TokenExpired(Class<?> tokenType, User user) {
        this.tokenType = tokenType;
        this.userNameOrLogin = user.getUserLogin() != null && !user.getUserLogin().isBlank() ? user.getUserLogin() :
                user.getName() != null && !user.getName().isBlank() ?
                        user.getName() :
                        null;
    }

    @Override
    public ApiException get() {

        String message = "";

        if (tokenType.isAssignableFrom(VerificationToken.class))
            message = "Срок действия токена подтверждения почты истёк! Либо пользователь уже подтвердил почту.";

        if (tokenType.isAssignableFrom(PasswordResetToken.class))
            message = "Срок действия токена для восстановления пароля истёк, либо данный пользователь не сбрасывал пароль!";

        return new ApiException(message);
    }
}
