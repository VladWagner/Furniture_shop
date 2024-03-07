package gp.wagner.backend.validation.producer_request_dto.validators;

import gp.wagner.backend.domain.dto.request.crud.ProducerRequestDto;
import gp.wagner.backend.validation.producer_request_dto.annotations.ValidProducerRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ProducerRequestDtoValidator implements ConstraintValidator<ValidProducerRequestDto, ProducerRequestDto> {

    @Override
    public void initialize(ValidProducerRequestDto constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    // Провести валидацию полей передаваемого DTO производителя
    @Override
    public boolean isValid(ProducerRequestDto dto,
                           ConstraintValidatorContext constraintValidatorContext) {

        if (!dto.getIsShown() && (dto.getIsDisclosed() != null && dto.getIsDisclosed())) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("Флаг раскрытия задан некорректно, поскольку isShown стоит в false!")
                    .addConstraintViolation();
            return false;
        }
        else if (dto.getIsShown() && (dto.getIsDisclosed() != null && dto.getIsDisclosed()) && dto.getDiscloseHeirs() == null) {

            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("Если производитель был восстановлен в показе, " +
                            "тогда нужно установить флаг восстановления всех связанных с ним сущностей (товары и варианты)!")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
