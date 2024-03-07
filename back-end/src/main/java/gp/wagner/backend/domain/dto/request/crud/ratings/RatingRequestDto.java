package gp.wagner.backend.domain.dto.request.crud.ratings;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.validation.user_request_dto.annotations.ValidUserRequestDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Объект для добавления/редактирования оценки товара
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidUserRequestDto
public class RatingRequestDto {

    @Nullable
    private Long id;

    @NotNull
    private Integer rating;

    @NotNull
    @JsonProperty("product_id")
    private Integer productId;

    @NotNull
    @JsonProperty("user_id")
    private Long userId;

}

