package gp.wagner.backend.domain.entities.products;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.Date;
import java.util.List;
import java.util.Objects;

// Скидка
@Entity
@Table(name = "discounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // % скидки
    @Column(name = "percentage")
    private Float percentage;

    // Дата начала срока действия скидки
    @Column(name = "starts_at")
    private Date startsAt;

    // Дата окончания срока действия скидки
    @Column(name = "ends_at")
    private Date endsAt;

    // Флаг активна/неактивна скидка
    @Column(name = "is_active")
    private Boolean isActive;

    // Флаг бессрочности действия скидки
    @Column(name = "is_infinite")
    private Boolean isInfinite;

    // Варианты, в которых задана данная скидка
    @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY)
    @BatchSize(size = 256)
    private List<ProductVariant> productVariants;

    // Деактивировать скидку срок действия которой закончился
    public void deactivateExpiredDiscount(){
        // Можно было написать в одну строку, но чтобы лишний раз не менять корректную сущность используется ветвление
        if(isExpired())
            this.isActive = false;
    }

    public boolean isExpired(){
        // Если скидка бессрочная или уже деактивирована
        if (this.isInfinite || !this.isActive)
            return false;

        Date now = new Date();

        return endsAt.getTime() <= now.getTime();
    }

    public Discount(Long id, Float percentage, Date startsAt, Date endsAt, boolean isActive, boolean isInfinite) {
        this.id = id;
        this.percentage = percentage;
        this.startsAt = !isInfinite ? startsAt : null;
        this.endsAt = !isInfinite ? endsAt : null;
        this.isActive = isActive;
        this.isInfinite = isInfinite;
    }


    // Проверить равенство по диапазону дат срока действия и по %
    public boolean isEqual(Discount discount){
        if (discount == null)
            return false;

        return Objects.equals(this.id, discount.id) &&
               Objects.equals(this.startsAt, discount.startsAt) &&
               Objects.equals(this.percentage, discount.percentage);
    }
}
