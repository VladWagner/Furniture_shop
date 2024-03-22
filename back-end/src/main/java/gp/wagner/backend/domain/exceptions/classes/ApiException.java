package gp.wagner.backend.domain.exceptions.classes;

import gp.wagner.backend.infrastructure.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.nio.charset.StandardCharsets;

//Наследуемся от RuntimeException, чтобы можно было пробрасывать исключение дальше в обработчике
public class ApiException extends RuntimeException {

    private HttpStatusCode errorType = HttpStatus.INTERNAL_SERVER_ERROR;

    public ApiException(String message) {

        //Проверить кодировку строки и если charset == UTF_8, тогда ничего не делать
        super(Utils.checkCharset(message, StandardCharsets.UTF_8) ?
                message :
                new String(message.getBytes(StandardCharsets.UTF_8)));
    }

    public ApiException(String message, HttpStatus errorCode) {

        //Проверить кодировку строки и если charset == UTF_8, тогда ничего не делать
        super(Utils.checkCharset(message, StandardCharsets.UTF_8) ?
                message :
                new String(message.getBytes(StandardCharsets.UTF_8)));

        errorType = errorCode;
    }

    public HttpStatusCode getErrorType() {
        return errorType;
    }
}
