package gp.wagner.backend.domain.entites.products;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

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

    // Адрес изображения для предосмотра
    @Column(name = "preview_img")
    private String previewImg;

    // Наименование варианта
    @Column(name = "title")
    private String title;

    // Связующие свойство товара (Многие варианты для 1 товара)
    @ManyToOne()
    @JoinColumn(name = "product_id")
    private Product product;

    // Стоимости данного варианта
    @Column(name = "price")
    private int price;

    @ManyToOne()
    @JoinColumn(name = "discount_id")
    private Discount discount;

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

    //Варианты товаров в заказах
    /*@OneToMany(mappedBy = "productVariant")
    @BatchSize(size = 256)
    private List<OrderAndProductVariant> orderAndPVList;*/

    public ProductVariant(Long id, String title, Product product, int price, String previewImg, boolean showVariant, List<ProductImage> productImages) {
        this.id = id;
        this.previewImg = previewImg;
        this.title = title;
        this.product = product;
        this.price = price;
        this.showVariant = showVariant;
        this.productImages = productImages;
    }

    // Получить цену со скидкой
    public int getPriceWithDiscount(){

        // Если скидка не задана или задана некорректно, или больше неактивна, тогда вернуть старую цену
        if (discount == null || discount.getPercentage() == null || !discount.getIsActive() || discount.isExpired() || discount.getPercentage() > 0.999)
            return this.price;

        int discountPart = Math.round(this.price * discount.getPercentage());
        return this.price - discountPart;
    }
}


