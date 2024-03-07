package gp.wagner.backend.validation.user_request_dto.validators;

import com.ctc.wstx.shaded.msv_core.datatype.xsd.regex.RegExp;
import gp.wagner.backend.domain.dto.request.crud.user.UserRequestDto;
import gp.wagner.backend.infrastructure.Constants;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.validation.user_request_dto.annotations.ValidUserRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserRequestDtoValidator implements ConstraintValidator<ValidUserRequestDto, UserRequestDto> {


    @Override
    public void initialize(ValidUserRequestDto constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    // Если определённые свойства не заданы и без них DTO не консистентен
    @Override
    public boolean isValid(UserRequestDto dto,
                           ConstraintValidatorContext constraintValidatorContext) {

        if (!Utils.emailIsValid(dto.getEmail())){
            String message = "Email пользователя задан некорректно!";

            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            return false;
        }


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
