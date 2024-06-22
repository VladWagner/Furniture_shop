package gp.wagner.backend.domain.exceptions.classes;

import gp.wagner.backend.infrastructure.Utils;

import java.nio.charset.StandardCharsets;

public class JwtValidationException extends RuntimeException {

    private final int errorType;

    private final static int tokenNotValidStatus = 442;

    public JwtValidationException(String message) {

        //Проверить кодировку строки и если charset == UTF_8, тогда ничего не делать
        super(Utils.checkCharset(message, StandardCharsets.UTF_8) ?
                message :
                new String(message.getBytes(StandardCharsets.UTF_8)));

        // Если токен просрочен, тогда 444, в остальных случая - 442
        errorType = message.contains("JWT expired") ? 444 : tokenNotValidStatus;
    }

    /*public HttpStatusCode getErrorType() {
        return errorType;
    }*/
    public int getErrorType() {
        return errorType;
    }

    public static int getTokenNotValidStatus() {
        return tokenNotValidStatus;
    }
}
