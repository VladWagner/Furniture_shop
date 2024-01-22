package gp.wagner.backend.validation.order_request_dto.validators;

import gp.wagner.backend.domain.dto.request.admin_panel.OrdersAndBasketsCountFiltersRequestDto;
import gp.wagner.backend.domain.dto.request.crud.OrderRequestDto;
import gp.wagner.backend.validation.order_request_dto.annotations.ValidOrderRequestDto;
import gp.wagner.backend.validation.orders_couting_filters_dto.annotations.ValidOrdersCountingFiltersRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OrderRequestDtoValidator implements ConstraintValidator<ValidOrderRequestDto, OrderRequestDto> {

    @Override
    public void initialize(ValidOrderRequestDto constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    // Если определённые свойства не заданы и без них DTO не консистентен
    @Override
    public boolean isValid(OrderRequestDto dto,
                           ConstraintValidatorContext constraintValidatorContext) {

        if (dto.getId() == null && dto.getCustomer() == null && (dto.getCode() == null || dto.getCode() <= 0)) {

            String message = """
                    Dto для редактирования заказа с id %d задан некорректно!
                    Если Customer не задан, то должен быть задан код или хотя бы id заказа""";

            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
