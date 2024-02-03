package gp.wagner.backend.domain.exception.suppliers;

import gp.wagner.backend.domain.exception.ApiException;
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
public class UserNotFound implements Supplier<ApiException> {

    // id пользователя для сообщения
    private Long userId;

    // Email, по которому пользователь мог быть не найден
    private String email;

    private String login;

    public UserNotFound(Long userId) {
        this.userId = userId;
    }

    public UserNotFound(String email, String login) {
        this.email = email;
        this.login = login;
    }

    @Override
    public ApiException get() {

        StringBuilder sb = new StringBuilder("Пользователь ");

        boolean userNotNull = userId != null;
        boolean loginNotNull = login != null;

        if (userNotNull)
            sb.append(String.format("с id %d ", userId));

        // Если задан и id пользователя и логин, тогда добавить союз «и»
        if (loginNotNull)
            sb.append(String.format("%2$s логином %1$s ", login, userNotNull ? "и" : "с"));

        // Если задан и id пользователя и логин и ещё email, тогда добавить союз «и»
        if (email != null)
            sb.append(String.format("%2$s email %1$s ", email, userNotNull || loginNotNull ? "и" : "c"));


        return new ApiException(sb.append("не найден!").toString());
    }
}
