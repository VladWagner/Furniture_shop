package gp.wagner.backend.domain.exception;

import gp.wagner.backend.infrastructure.Utils;

import java.nio.charset.StandardCharsets;

//Наследуемся от RuntimeException, чтобы можно было пробрасывать исключение дальше в обработчике
public class ApiException extends RuntimeException {

    public ApiException(String message) {

        //Проверить кодировку строки и если charset == UTF_8, тогда ничего не делать
        super(Utils.checkCharset(message, StandardCharsets.UTF_8) ?
                message :
                new String(message.getBytes(StandardCharsets.UTF_8)));
    }
}
