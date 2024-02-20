package gp.wagner.backend.domain.exceptions;


import lombok.*;

import java.util.Arrays;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionDto {

    private Class<?> exceptionType;

    private String message;

    private String stackTrace;

    public static ExceptionBuilder getBuilder(){
        return new ExceptionBuilder(new ExceptionDto());
    }

    // Построитель DTO
    @AllArgsConstructor
    public static class ExceptionBuilder {
        private ExceptionDto dto;

        public ExceptionBuilder exceptionType(Class<?> type){

            dto.exceptionType = type;

            return this;
        }
        public ExceptionBuilder message(String message){
            dto.message = message;

            return this;
        }
        public ExceptionBuilder stackTrace(String trace){
            dto.stackTrace = trace;

            return this;
        }

        public ExceptionDto build(){
            return dto;
        }

    }

    public <T extends Exception> ExceptionDto(T exception) {
        this.exceptionType = exception.getClass();
        this.message = exception.getMessage();

        // Использовать только во время разработки для отладки.
        this.stackTrace = Arrays.toString(exception.getStackTrace());
    }
}
