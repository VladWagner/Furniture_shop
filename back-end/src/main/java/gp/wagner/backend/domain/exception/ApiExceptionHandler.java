package gp.wagner.backend.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

//Перехватчик и обработчик исключения для последующей отправки его клиенту
@ControllerAdvice
public class ApiExceptionHandler  {

    //Обработка исключения
    @ExceptionHandler(value = {ApiException.class})
    public ResponseEntity<Object> handleException(Exception e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
