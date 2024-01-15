package gp.wagner.backend.domain.dto.response;

import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.validation.producer_request_dto.annotations.ValidProducerRequestDto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

//Объект для добавления производителя
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProducerRespDto {

    private long id;

    // Наименование производителя
    private String producerName;

    // Изображение производителя
    private String logoLink;

    // Флаг удаления
    private Date deletedAt;

    // Флаг вывода
    private Boolean isShown;

    public ProducerRespDto(Producer producer) {
        this.id = producer.getId();
        this.producerName = producer.getProducerName();
        this.logoLink = producer.getProducerLogo();
        this.deletedAt = producer.getDeletedAt();
        this.isShown = producer.getIsShown();
    }
}
