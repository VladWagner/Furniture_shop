package gp.wagner.backend.validation.review_request_dto.validators;

import gp.wagner.backend.domain.dto.request.crud.reviews.ReviewRequestDto;
import gp.wagner.backend.validation.review_request_dto.annotations.ValidReviewRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ReviewRequestDtoValidator implements ConstraintValidator<ValidReviewRequestDto, ReviewRequestDto> {

    @Override
    public void initialize(ValidReviewRequestDto constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    // Если определённые свойства не заданы и без них DTO не консистентен
    @Override
    public boolean isValid(ReviewRequestDto dto,
                           ConstraintValidatorContext constraintValidatorContext) {

        // При создании отзыва номер заказа не задан
        if (dto.getId() == null && dto.getOrderCode() == null) {

            String message = """
                    Dto для создания отзыва некорректен! Должен быть задан номер заказа.
                    """;

            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
