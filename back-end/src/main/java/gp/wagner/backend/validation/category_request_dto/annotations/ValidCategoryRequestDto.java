package gp.wagner.backend.validation.category_request_dto.annotations;

import gp.wagner.backend.validation.category_request_dto.validators.CategoryRequestDtoValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CategoryRequestDtoValidator.class)
public @interface ValidCategoryRequestDto {

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String message() default "Передаваемый DTO невалиден!";

}
