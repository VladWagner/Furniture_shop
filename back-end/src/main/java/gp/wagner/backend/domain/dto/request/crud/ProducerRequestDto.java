package gp.wagner.backend.domain.dto.request.crud;

import gp.wagner.backend.validation.producer_request_dto.annotations.ValidProducerRequestDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

//Объект для добавления производителя
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidProducerRequestDto
public class ProducerRequestDto {

    //Для редактирования производителя
    @Nullable
    private Long id;

    // Наименование производителя
    @NotNull
    @NotEmpty
    private String producerName;

    // Флаг удаления
    @Nullable
    private Boolean deleted;

    // Флаг вывода
    @Nullable
    private Boolean isShown;

    // Был ли восстановлен производитель из скрытия
    @Nullable
    private Boolean isDisclosed = false;

    // Флаг восстановления связанных записей
    @Nullable
    private Boolean discloseHeirs = false;

}
