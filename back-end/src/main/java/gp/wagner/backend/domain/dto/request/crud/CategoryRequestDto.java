package gp.wagner.backend.domain.dto.request.crud;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.validation.category_request_dto.annotations.ValidCategoryRequestDto;
import gp.wagner.backend.validation.producer_request_dto.annotations.ValidProducerRequestDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

//Объект для добавления/редактирования категории
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidCategoryRequestDto
public class CategoryRequestDto {

    @Nullable
    private Long id;

    // Название категории
    @Nullable
    @JsonProperty("category_name")
    private String categoryName;

    @Nullable
    @JsonProperty("parent_id")
    private Long parentId;

    // Флаг вывода
    @Nullable
    @JsonProperty("is_shown")
    private Boolean isShown;

    // Была ли восстановлена категория
    @Nullable
    @JsonProperty("is_disclosed")
    private Boolean isDisclosed = false;

    // Флаг восстановления связанных записей
    @Nullable
    @JsonProperty("disclose_heirs")
    private Boolean discloseHeirs = false;

}
