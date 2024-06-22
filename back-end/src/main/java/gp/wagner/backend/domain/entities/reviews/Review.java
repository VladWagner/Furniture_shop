package gp.wagner.backend.domain.entities.reviews;

import gp.wagner.backend.domain.entities.products.Product;
import gp.wagner.backend.domain.entities.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.Date;
import java.util.List;

// Отзыв на товар
@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Текст отзыва
    @Column(name = "text")
    private String text;

    // Связующее свойство пользователя добавившего отзыв
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Связующее свойство товара
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Флаг прохождения модерации отзыва
    @Column(name = "is_verified")
    private Boolean isVerified;

    // Дата создания отзыва
    @Column(name = "created_at")
    private Date createdAt;

    // Дата последнего обновления
    @Column(name = "updated_at")
    private Date updatedAt;

    // Дата удаления отзыва
    @Column(name = "deleted_at")
    private Date deletedAt;

    // Изображения данного отзыва
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "review")
    @BatchSize(size = 256)
    private List<ReviewImage> reviewImages;

    public Review(String text, User user, Product product, Boolean isVerified) {
        this.text = text;
        this.user = user;
        this.product = product;
        this.isVerified = isVerified;
    }
}
