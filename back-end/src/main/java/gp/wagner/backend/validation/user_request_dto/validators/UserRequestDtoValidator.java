package gp.wagner.backend.validation.user_request_dto.validators;

import gp.wagner.backend.domain.dto.request.crud.user.UserRequestDto;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.validation.user_request_dto.annotations.ValidUserRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;

public class UserRequestDtoValidator implements ConstraintValidator<ValidUserRequestDto, UserRequestDto> {

    @Override
    public void initialize(ValidUserRequestDto constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    // Если определённые свойства не заданы и без них DTO не консистентен
    @Override
    public boolean isValid(UserRequestDto dto,
                           ConstraintValidatorContext constraintValidatorContext) {

        if (dto.getId() == null && dto.getPassword() == null || dto.getPassword().isBlank()) {

            String message = """
                    Dto для создания пользователя задан некорректно! Поле пароля не может быть пустым.""";

            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
             return false;
        }

        return true;
    }
}
