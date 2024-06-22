package gp.wagner.backend.domain.exceptions;

import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.domain.exceptions.classes.JwtValidationException;
import gp.wagner.backend.domain.exceptions.classes.UserNotConfirmedException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.List;

// Перехватчик и обработчик исключения для последующей отправки его клиенту
@ControllerAdvice
public class ExceptionsHandler {

    //Обработка общего исключения
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ExceptionDto> handleException(Exception e){

        ExceptionDto exceptionDto = ExceptionDto.getBuilder()
                .exceptionType(Exception.class)
                .message(e.getMessage())
                .stackTrace(Arrays.toString(e.getStackTrace()))
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exceptionDto);
    }

    //Обработка общего API исключения
    @ExceptionHandler(value = {ApiException.class})
    public ResponseEntity<ExceptionDto> handleApiException(ApiException e){

        ExceptionDto exceptionDto = ExceptionDto.getBuilder()
                .exceptionType(ApiException.class)
                .message(e.getMessage())
                .stackTrace(Arrays.toString(e.getStackTrace()))
                .build();

        return ResponseEntity
                .status(e.getErrorType())
                .body(exceptionDto);
    }


    // Обработка при валидации токена аутентификации и авторизации
    @ExceptionHandler(value = {JwtValidationException.class})
    public ResponseEntity<ExceptionDto> handleJwtValidationException(JwtValidationException e){

        ExceptionDto exceptionDto = ExceptionDto.getBuilder()
                .exceptionType(JwtValidationException.class)
                .message(e.getMessage())
                //.stackTrace(Arrays.toString(e.getStackTrace()))
                .build();

        return ResponseEntity
                .status(e.getErrorType())
                .body(exceptionDto);
    }

    // Обработка исключения при отсутствии прав у авторизированного пользователя
    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<ExceptionDto> handleAccessDeniedException(AccessDeniedException e){

        boolean permissionsException = e.getMessage().contains("permissions");

        ExceptionDto exceptionDto = ExceptionDto.getBuilder()
                .exceptionType(AccessDeniedException.class)
                .message(String.format("%s: %s", permissionsException ? "У вас недостаточно прав" : "Вы не вошли в аккаунт", e.getMessage()))
                //.stackTrace(Arrays.toString(e.getStackTrace()))
                .build();

        return ResponseEntity
                .status(permissionsException ? 443 : HttpStatus.FORBIDDEN.value())
                .body(exceptionDto);
    }

    //Обработка исключения при регистрации/авторизации пользователя
    @ExceptionHandler(value = {UserNotConfirmedException.class})
    public ResponseEntity<ExceptionDto> handleUserNotConfirmedException(UserNotConfirmedException e){

        ExceptionDto exceptionDto = ExceptionDto.getBuilder()
                .exceptionType(UserNotConfirmedException.class)
                .message(e.getMessage())
                //.stackTrace(Arrays.toString(e.getStackTrace()))
                .build();

        return ResponseEntity
                .status(e.getErrorType())
                .body(exceptionDto);
    }


    // Обработка constraint исключения связанного с валидацией передаваемых DTO сущностей
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ExceptionDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){

        // Получить сообщения из результатов привязки
        BindingResult bindingResult = e.getBindingResult();

        // Сформировать сообщение из ошибок
        StringBuilder sbErrors = new StringBuilder();
        List<ObjectError> objectErrors = bindingResult.getGlobalErrors();

        for (ObjectError field : objectErrors){
            sbErrors.append(field.getDefaultMessage()).append('\n');
        }

        ExceptionDto exceptionDto = ExceptionDto.getBuilder()
                .exceptionType(ConstraintViolationException.class)
                .message(sbErrors.toString())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exceptionDto);
    }

    // Обработка constraint исключения связанного с валидацией
    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<ExceptionDto> handleConstraintViolationException(ConstraintViolationException e){

        String exceptionMessage = e.getConstraintViolations() != null ?
                e.getConstraintViolations().iterator().next().getMessage():
                e.getMessage();
        ExceptionDto exceptionDto = ExceptionDto.getBuilder()
                .exceptionType(ConstraintViolationException.class)
                .message(exceptionMessage)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exceptionDto);
    }

}
