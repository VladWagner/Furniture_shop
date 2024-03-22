package gp.wagner.backend.domain.exceptions.classes;

import gp.wagner.backend.infrastructure.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.nio.charset.StandardCharsets;

public class JwtValidationException extends RuntimeException {

    private final int errorType;

    public JwtValidationException(String message) {

        //Проверить кодировку строки и если charset == UTF_8, тогда ничего не делать
        super(Utils.checkCharset(message, StandardCharsets.UTF_8) ?
                message :
                new String(message.getBytes(StandardCharsets.UTF_8)));

        errorType = 444;
    }

    /*public HttpStatusCode getErrorType() {
        return errorType;
    }*/
    public int getErrorType() {
        return errorType;
    }
}
