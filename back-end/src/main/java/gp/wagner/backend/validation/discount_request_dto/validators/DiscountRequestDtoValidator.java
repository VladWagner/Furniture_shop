package gp.wagner.backend.validation.discount_request_dto.validators;

import gp.wagner.backend.domain.dto.request.crud.DiscountRequestDto;
import gp.wagner.backend.validation.discount_request_dto.annotations.ValidDiscountRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DiscountRequestDtoValidator implements ConstraintValidator<ValidDiscountRequestDto, DiscountRequestDto> {

    @Override
    public void initialize(ValidDiscountRequestDto constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    // Если определённые свойства не заданы и без них DTO не консистентен
    @Override
    public boolean isValid(DiscountRequestDto dto,
                           ConstraintValidatorContext constraintValidatorContext) {

        boolean basicFieldsEqualsNull = dto.getPercentage() == null &&
                dto.getStartsAt() == null && dto.getEndsAt() == null;

        boolean addingListsAreNullOrEmpty = (dto.getProductsVariantsIds() == null || dto.getProductsVariantsIds().isEmpty()) &&
                (dto.getProductsIds() == null || dto.getProductsIds().isEmpty()) && dto.getCategoryId() == null;

        boolean removalsListsAreNullOrEmpty = (dto.getRemovedVariantsIds() == null || dto.getRemovedVariantsIds().isEmpty()) &&
                (dto.getRemovedProductsIds() == null || dto.getRemovedProductsIds().isEmpty()) &&
                (dto.getRemovedCategoriesIds() == null || dto.getRemovedCategoriesIds().isEmpty());

        // Если создаётся скидка и базовые поля не заданы
        if (dto.getId() == null && basicFieldsEqualsNull) {

            String message = """
                    Dto для создания скидки задан некорректно!
                    Должны быть заданы все базовые значения скидки: % скидки, дата начала и окончания действия""";

            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            return false;
        }
        // Если скидка редактируется и при этом не заданы базовые поля и не заданы списки для добавления/удаления скидки
        else if (dto.getId() != null && dto.getId() > 0 && basicFieldsEqualsNull
                && addingListsAreNullOrEmpty && removalsListsAreNullOrEmpty) {

            String message = String.format("""
                    Dto для редактирования скидки с id %d задан некорректно!
                    Должен быть задан хотя бы один из списков добавления/удаления скидки""", dto.getId());

            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            return false;
        }

        // Если при создании скидки флаг бессрочности скидки не задан и при этом даты начала и окончания срока действия заданы некорректно
        if ((dto.getId() == null || dto.getId() <= 0) && (dto.getIsInfinite() == null || !dto.getIsInfinite() ) && ((dto.getStartsAt() == null || dto.getEndsAt() == null) ||
                dto.getStartsAt().after(dto.getEndsAt()))
        ) {

            String message = """
                    Dto для создания скидки задан некорректно!
                    Поскольку скидка не бессрочная, тогда даты начала и окончания должны быть заданы корректно""";

            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            return false;
        }
        // Если при создании скидки флаг бессрочности скидки не задан и при этом даты начала и окончания срока действия заданы некорректно
        if ((dto.getId() != null && dto.getId() > 0) && (dto.getIsInfinite() == null || !dto.getIsInfinite())  && (dto.getStartsAt() != null && dto.getEndsAt() != null &&
                dto.getStartsAt().after(dto.getEndsAt()))
        ){

            String message = """
                    Dto для изменения скидки задан некорректно!
                    Поскольку скидка не бессрочная, тогда даты начала и окончания должны быть заданы корректно""";

            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
