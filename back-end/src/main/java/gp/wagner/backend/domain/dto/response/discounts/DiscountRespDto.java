package gp.wagner.backend.domain.dto.response.discounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gp.wagner.backend.domain.entites.products.Discount;
import gp.wagner.backend.infrastructure.serializers.DateTimeJsonSerializer;
import jakarta.persistence.Column;
import lombok.*;

import java.util.Date;

//Объект для добавления производителя
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscountRespDto {

    private long id;

    // % скидки
    private Float percentage;

    @JsonProperty("starts_at")
    @JsonSerialize(using = DateTimeJsonSerializer.class)
    private Date startsAt;

    @JsonProperty("ends_at")
    @JsonSerialize(using = DateTimeJsonSerializer.class)
    private Date endsAt;

    // Флаг активна/неактивна скидка
    @JsonProperty("is_active")
    private boolean isActive;

    // Флаг бессрочности действия скидки
    @JsonProperty("is_infinite")
    private Boolean isInfinite;

    public DiscountRespDto(Discount discount) {
        this.id = discount.getId();
        this.percentage = discount.getPercentage();
        this.startsAt = discount.getStartsAt();
        this.endsAt = discount.getEndsAt();
        this.isActive = discount.getIsActive();
        this.isInfinite = discount.getIsInfinite();
    }
}