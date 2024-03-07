package gp.wagner.backend.domain.entites.reviews;

import gp.wagner.backend.domain.entites.products.ProductVariant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Изображение в отзыве
@Entity
@Table(name = "reviews_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связующее свойство с отзывом (много изображений к одному отзыву)
    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    // Адрес на изображение на сервере
    @Column(name = "img_link")
    private String imgLink;

    // Порядок вывода данного изображения в отзыве
    @Column(name = "img_order")
    private Integer imgOrder;

    public ReviewImage(Review review, String imgLink, Integer imgOrder) {
        this.review = review;
        this.imgLink = imgLink;
        this.imgOrder = imgOrder;
    }
}
