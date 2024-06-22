package gp.wagner.backend.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entities.visits.Visitor;
import lombok.*;

import java.util.Date;

//Объект передачи и вывода товара в списке товаров в виде карточки
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VisitorRespDto {

    private Long id;

    //Отпечаток
    private String fingerprint;

    @JsonProperty("created_at")
    private Date createdAt;

    // Дата последнего посещения
    @JsonProperty("last_visit")
    private Date lastVisit;

    public VisitorRespDto(Visitor visitor) {
        this.id = visitor.getId();
        this.fingerprint = visitor.getFingerprint();
        this.createdAt = visitor.getCreatedAt();
        this.lastVisit = visitor.getLastVisit();
    }
}
