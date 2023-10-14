package gp.wagner.backend.domain.entites.products;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
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

    //Изображения данного варианта (Один вариант к многим товарам)
    //@OneToMany
    //private List<ProductImage> productImages = new ArrayList<>();

    public ProductVariant(String title, Product product, int price, String previewImg, boolean showVariant) {
        this.previewImg = previewImg;
        this.title = title;
        this.product = product;
        this.price = price;
        this.showVariant = showVariant;
    }
}
