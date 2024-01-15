package gp.wagner.backend.domain.exception;

import gp.wagner.backend.validation.producer_request_dto.exceptions.ProducerDisclosureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

//Перехватчик и обработчик исключения для последующей отправки его клиенту
@ControllerAdvice
public class ApiExceptionHandler  {

    //Обработка общего исключения
    @ExceptionHandler(value = {ApiException.class})
    public ResponseEntity<Object> handleException(Exception e){

        ExceptionDto exceptionDto = ExceptionDto.getBuilder()
                .exceptionType(ApiException.class)
                .message(e.getMessage())
                .build();

        return new ResponseEntity<>(exceptionDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Обработка исключения связанного с ProducerRequestDto
    @ExceptionHandler(value = {ProducerDisclosureException.class})
    public ResponseEntity<Object> handleProducerRequestDtoException(Exception e){

        ExceptionDto exceptionDto = ExceptionDto.getBuilder()
                .exceptionType(ProducerDisclosureException.class)
                .message(e.getMessage())
                .build();

        return new ResponseEntity<>(exceptionDto, HttpStatus.BAD_REQUEST);
    }

}
