package gp.wagner.backend.domain.entities.baskets;


import gp.wagner.backend.domain.entities.products.ProductVariant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//Корзина для авторизированных пользователей
@Entity
@Table(name = "baskets_products_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BasketAndProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Связующее свойство варианта товара
    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    //Количество заказываемых вариантов товара
    @Column(name = "products_count")
    private int productsAmount;

    // Связующее свойство с корзиной
    @ManyToOne
    @JoinColumn(name = "basket_id")
    private Basket basket;

}
