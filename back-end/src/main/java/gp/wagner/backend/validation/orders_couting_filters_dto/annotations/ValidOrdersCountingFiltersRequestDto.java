package gp.wagner.backend.validation.orders_couting_filters_dto.annotations;

import gp.wagner.backend.validation.orders_couting_filters_dto.validators.OrdersCountingFiltersRequestDtoValidator;
import gp.wagner.backend.validation.producer_request_dto.validators.ProducerRequestDtoValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrdersCountingFiltersRequestDtoValidator.class)
public @interface ValidOrdersCountingFiltersRequestDto {

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String message() default "Передаваемый DTO невалиден!";

}
