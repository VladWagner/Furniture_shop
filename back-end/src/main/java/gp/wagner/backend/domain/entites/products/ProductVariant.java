package gp.wagner.backend.domain.entites.products;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

//Варианты исполнения товара
@Entity
@Table(name = "variants_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Адрес изображения для предосмотра
    @Column(name = "preview_img")
    private String previewImg;

    //Описание варианта
    @Column(name = "title")
    private String title;

    //Связующие свойство категории товара (Многие варианты для 1 товара)
    @ManyToOne()
    @JoinColumn(name = "product_id")
    private Product product;

    //Стоимости данного варианта
    @Column(name = "price")
    private int price;

    //Стоимости данного варианта
    @Column(name = "show_variant")
    private Boolean showVariant;

    // Флаг удалён ли товар
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    //Изображения данного варианта (Один вариант к многим товарам)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productVariant")
    @BatchSize(size = 256)
    private List<ProductImage> productImages;

    public ProductVariant(Long id, String title, Product product, int price, String previewImg, boolean showVariant, List<ProductImage> productImages) {
        this.id = id;
        this.previewImg = previewImg;
        this.title = title;
        this.product = product;
        this.price = price;
        this.showVariant = showVariant;
        this.productImages = productImages;
    }
}


