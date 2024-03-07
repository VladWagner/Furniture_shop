package gp.wagner.backend.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.Utils;
import jakarta.persistence.Column;
import lombok.*;

import java.text.ParseException;
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
