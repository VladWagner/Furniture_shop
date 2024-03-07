package gp.wagner.backend.validation.discount_request_dto.annotations;

import gp.wagner.backend.validation.discount_request_dto.validators.DiscountRequestDtoValidator;
import gp.wagner.backend.validation.order_request_dto.validators.OrderRequestDtoValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DiscountRequestDtoValidator.class)
public @interface ValidDiscountRequestDto {

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String message() default "Передаваемый DTO невалиден!";

}
