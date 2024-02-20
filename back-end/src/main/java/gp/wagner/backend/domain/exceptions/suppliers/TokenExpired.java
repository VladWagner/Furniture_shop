package gp.wagner.backend.domain.exceptions.suppliers;

import gp.wagner.backend.domain.entites.tokens.PasswordResetToken;
import gp.wagner.backend.domain.entites.tokens.VerificationToken;
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
    private String userLogin;

    public TokenExpired(Class<?> tokenType) {
        this.tokenType = tokenType;
    }

    @Override
    public ApiException get() {

        String message = "";

        if (tokenType.isAssignableFrom(VerificationToken.class))
            message = String.format("Срок действия токена подтверждения почты истёк! Либо пользователь %s уже подтвердил почту.",
                    userLogin != null ? userLogin : "");

        if (tokenType.isAssignableFrom(PasswordResetToken.class))
            message = "Срок действия токена для восстановления пароля истёк, либо данный пользователь не сбрасывал пароль!";

        return new ApiException(message);
    }
}
