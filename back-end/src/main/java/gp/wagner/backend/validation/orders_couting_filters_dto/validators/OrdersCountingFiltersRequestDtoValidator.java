package gp.wagner.backend.validation.orders_couting_filters_dto.validators;

import gp.wagner.backend.domain.dto.request.admin_panel.OrdersAndBasketsCountFiltersRequestDto;
import gp.wagner.backend.validation.orders_couting_filters_dto.annotations.ValidOrdersCountingFiltersRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OrdersCountingFiltersRequestDtoValidator implements ConstraintValidator<ValidOrdersCountingFiltersRequestDto, OrdersAndBasketsCountFiltersRequestDto> {

    @Override
    public void initialize(ValidOrdersCountingFiltersRequestDto constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    // Проверка консистентности свойств
    @Override
    public boolean isValid(OrdersAndBasketsCountFiltersRequestDto dto,
                           ConstraintValidatorContext constraintValidatorContext) {

        if (dto.getMinDate() != null && dto.getMaxDate() != null && !dto.areDatesCorrect()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("Диапазон дат задан неверно. Начальная дата не может быть > конечной!")
                    .addConstraintViolation();
            return false;
        }
        else if (dto.getPriceMin() != null && dto.getPriceMax() != null && !dto.arePricesCorrect()) {

            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("Диапазон цен задан неверно. Минимальное значение не может быть > максимального!")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
