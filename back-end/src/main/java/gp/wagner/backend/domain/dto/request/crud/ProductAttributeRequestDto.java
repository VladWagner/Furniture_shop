package gp.wagner.backend.domain.dto.request.crud;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

// DTO атрибута товара - характеристика
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeRequestDto {

    // Для редактирования
    @Nullable
    private Long id;

    // Название атрибута
    @NotNull
    private String name;

    // Категории, к которым будет принадлежать атрибут (если значение не задано, тогда будет принадлежать ко всем категориям)
    @Nullable
    @JsonProperty("categories_ids")
    private List<Long> categoriesIds;


    // Флаг показа атрибута
    @Nullable
    @JsonProperty("is_shown")
    private Boolean isShown = true;


}
