package gp.wagner.backend.domain.entities.ratings;

import gp.wagner.backend.domain.entities.products.Product;
import gp.wagner.backend.domain.entities.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

// Оценка конкретного товара
@Entity
@Table(name = "ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Оценка
    @JoinColumn(name = "rating")
    private Integer rating;

    // Связующее свойство пользователя установившего оценку
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Связующее свойство товара
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Дата создания
    @Column(name = "created_at")
    private Date createdAt;

    // Дата последнего обновления
    @Column(name = "updated_at")
    private Date updatedAt;

    public Rating(Integer rating, User user, Product product) {
        this.rating = rating;
        this.user = user;
        this.product = product;
    }
}
