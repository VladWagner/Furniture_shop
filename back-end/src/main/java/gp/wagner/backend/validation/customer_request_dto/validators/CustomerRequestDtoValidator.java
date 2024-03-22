package gp.wagner.backend.validation.customer_request_dto.validators;

import gp.wagner.backend.domain.dto.request.crud.CustomerRequestDto;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.validation.customer_request_dto.annotations.ValidCustomerRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CustomerRequestDtoValidator implements ConstraintValidator<ValidCustomerRequestDto, CustomerRequestDto> {


    @Override
    public void initialize(ValidCustomerRequestDto constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    // Если определённые свойства не заданы и без них DTO не консистентен
    @Override
    public boolean isValid(CustomerRequestDto dto,
                           ConstraintValidatorContext constraintValidatorContext) {

        if (!Utils.emailIsValid(dto.getEmail())){
            String message = "Email покупателя задан некорректно!";

            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            return false;
        }


        if (dto.getId() == null && dto.getPhoneNumber() == null || dto.getPhoneNumber() <= 0) {

            String message = """
                    Dto для создания покупателя задан некорректно! Номер телефона - обязательное поле!""";

            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
             return false;
        }

        return true;
    }

}
