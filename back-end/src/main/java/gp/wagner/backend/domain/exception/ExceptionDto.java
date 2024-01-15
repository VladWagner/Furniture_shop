package gp.wagner.backend.domain.exception;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

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
            dto.message = trace;

            return this;
        }

        public ExceptionDto build(){
            return dto;
        }

    }


}
