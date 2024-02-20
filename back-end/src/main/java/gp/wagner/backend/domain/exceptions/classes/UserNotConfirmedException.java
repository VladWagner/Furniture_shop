package gp.wagner.backend.domain.exceptions.classes;

import gp.wagner.backend.infrastructure.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.nio.charset.StandardCharsets;

public class UserNotConfirmedException extends RuntimeException {

    // Код ошибки - нужен для фронта (500 - ошибка авторизации, а 512 - ошибка при регистрации)
    private final HttpStatusCode INTERNAL_ERROR = HttpStatus.INTERNAL_SERVER_ERROR;
    private final HttpStatusCode CONFIRMATION_WHILE_REGISTRATION_ERROR = HttpStatusCode.valueOf(512);

    private final HttpStatusCode errorType;

    public UserNotConfirmedException(String message) {

        //Проверить кодировку строки и если charset == UTF_8, тогда ничего не делать
        super(Utils.checkCharset(message, StandardCharsets.UTF_8) ?
                message :
                new String(message.getBytes(StandardCharsets.UTF_8)));

        errorType = INTERNAL_ERROR;
    }
    public UserNotConfirmedException() {

        // Сообщение по умолчанию
        super("Пользователь зарегистрирован, но почта не подтверждена. Есть возможность повторно отправить сообщение для подтверждения!");

        errorType = CONFIRMATION_WHILE_REGISTRATION_ERROR;
    }

    public HttpStatusCode getErrorType() {
        return errorType;
    }
}
