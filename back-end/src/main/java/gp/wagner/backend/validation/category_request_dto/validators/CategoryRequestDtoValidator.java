package gp.wagner.backend.validation.category_request_dto.validators;

import gp.wagner.backend.domain.dto.request.crud.CategoryRequestDto;
import gp.wagner.backend.validation.category_request_dto.annotations.ValidCategoryRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CategoryRequestDtoValidator implements ConstraintValidator<ValidCategoryRequestDto, CategoryRequestDto> {

    @Override
    public void initialize(ValidCategoryRequestDto constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    // Проверить, корректности задаваемого dto категории
    @Override
    public boolean isValid(CategoryRequestDto dto,
                           ConstraintValidatorContext constraintValidatorContext) {


        if (dto.getIsShown() != null && !dto.getIsShown() && (dto.getIsDisclosed() != null && dto.getIsDisclosed())) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("Флаг раскрытия задан некорректно, поскольку isShown стоит в false!")
                    .addConstraintViolation();
            return false;
        }
        else if (dto.getIsShown() != null && dto.getIsShown() && (dto.getIsDisclosed() != null && dto.getIsDisclosed()) &&
                dto.getDiscloseHeirs() == null) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("Если категория была восстановлена в показе, " +
                            "тогда нужно установить флаг восстановления всех связанных с ней сущностей (товары и варианты)!")
                    .addConstraintViolation();
            return false;
        } else if ((dto.getId() == null || dto.getId() <= 0) && (dto.getCategoryName() == null || dto.getCategoryName().isBlank())) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("Id и название категории не заданы, для создания записи " +
                            "должно быть задано название категории!")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
