package gp.wagner.backend.domain.entites.ratings;

import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.entites.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

// Статистика оценок конкретного товара
@Entity
@Table(name = "ratings_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RatingStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связующее свойство товара
    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Общая сумма оценок на товар
    @Column(name = "ratings_sum")
    private Integer sum;

    // Средняя оценка на товар
    @Column(name = "avg")
    private Float avg;

    // Количество заданных оценок товара
    @Column(name = "amount")
    private Integer amount;

    public RatingStatistics(Product product, Float avg, Integer amount) {
        this.product = product;
        this.avg = avg;
        this.amount = amount;
    }
}
