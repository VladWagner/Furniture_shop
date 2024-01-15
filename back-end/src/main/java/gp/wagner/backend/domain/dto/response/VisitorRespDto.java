package gp.wagner.backend.domain.dto.response;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import gp.wagner.backend.domain.entites.visits.Visitor;
import jakarta.persistence.Column;
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

    private Date createdAt;

    // Дата последнего посещения
    private Date lastVisit;

    public VisitorRespDto(Visitor visitor) {
        this.id = visitor.getId();
        this.fingerprint = visitor.getFingerprint();
        this.createdAt = visitor.getCreatedAt();
        this.lastVisit = visitor.getLastVisit();
    }
}
