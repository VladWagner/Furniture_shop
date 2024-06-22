package gp.wagner.backend.domain.entities.products;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

//Изображения вариантов товаров
@Entity
@Table(name = "products_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Связующие свойство варианта товара (Многие изображения к 1 варианту)
    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    //Адрес на изображение на сервере
    @Column(name = "img_link")
    private String imgLink;

    //Порядок вывода данного изображения в галерее карточки варианта товара
    @Column(name = "img_order")
    private int imgOrder;


}
