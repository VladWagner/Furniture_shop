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
                //.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .status(e.getErrorType())
                .body(exceptionDto);
    }


    // Обработка при валидации токена аутентификации и авторизации
    @ExceptionHandler(value = {JwtValidationException.class})
    public ResponseEntity<ExceptionDto> handleJwtValidationException(JwtValidationException e){

        ExceptionDto exceptionDto = ExceptionDto.getBuilder()
                .exceptionType(JwtValidationException.class)
                .message(e.getMessage())
                .stackTrace(Arrays.toString(e.getStackTrace()))
                .build();

        return ResponseEntity
                .status(e.getErrorType())
                .body(exceptionDto);
    }

    // Обработка исключения при отсутствии прав у авторизированного пользователя
    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<ExceptionDto> handleAccessDeniedException(AccessDeniedException e){

        ExceptionDto exceptionDto = ExceptionDto.getBuilder()
                .exceptionType(AccessDeniedException.class)
                .message(String.format("У вас недостаточно прав или вы не вошли в аккаунт. %s", e.getMessage()))
                .stackTrace(Arrays.toString(e.getStackTrace()))
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(exceptionDto);
    }

    //Обработка исключения при регистрации/авторизации пользователя
    @ExceptionHandler(value = {UserNotConfirmedException.class})
    public ResponseEntity<ExceptionDto> handleUserNotConfirmedException(UserNotConfirmedException e){

        ExceptionDto exceptionDto = ExceptionDto.getBuilder()
                .exceptionType(UserNotConfirmedException.class)
                .message(e.getMessage())
                .stackTrace(Arrays.toString(e.getStackTrace()))
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

    /*@ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ValidationExceptionDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){

        // Получить сообщения из результатов привязки
        BindingResult bindingResult = e.getBindingResult();

        List<Violation> violations = bindingResult.getFieldErrors()
                .stream()
                .map(error -> new Violation(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ValidationExceptionDto(violations));
    }*/

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

    /*@ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<?> handleConstraintViolationExceptionViolationsDto(ConstraintViolationException e){

        List<Violation> violations = e.getConstraintViolations()
                .stream()
                .map(cv -> new Violation(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ValidationExceptionDto(violations));
    }*/

}
