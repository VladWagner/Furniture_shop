package gp.wagner.backend.domain.exception;

import gp.wagner.backend.validation.producer_request_dto.exceptions.ProducerDisclosureException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.List;

//Перехватчик и обработчик исключения для последующей отправки его клиенту
@ControllerAdvice
public class ApiExceptionHandler  {

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
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
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

        String exceptionMessage = e.getConstraintViolations().iterator().next().getMessage();
        ExceptionDto exceptionDto = ExceptionDto.getBuilder()
                .exceptionType(ConstraintViolationException.class)
                .message(exceptionMessage)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exceptionDto);
    }

}
