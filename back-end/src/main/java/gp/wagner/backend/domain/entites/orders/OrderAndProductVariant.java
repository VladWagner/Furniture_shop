package gp.wagner.backend.domain.entites.orders;

import gp.wagner.backend.domain.entites.products.ProductVariant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

// Заказ и варианты товаров.
// Данную сущность нельзя просто заменить связующим свойством ManyToMany, поскольку здесь хранится кол-во заказываемых товаров
@Entity
@Table(name = "orders_products_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderAndProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Количество заказываемых вариантов товара
    @Column(name = "products_count")
    private int productsAmount;

    //Связующее свойство варианта товара
    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    //Связующее свойство с заказом
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // Зафиксированная стоимость варианта товара
    @Column(name = "unit_price")
    private Integer unitPrice;

    public OrderAndProductVariant(Long id, int productsAmount, ProductVariant productVariant, Order order) {
        this.id = id;
        this.productsAmount = productsAmount;
        this.productVariant = productVariant;
        this.order = order;

        // Зафиксировать текущую цену варианта (со скидкой или без)
        this.unitPrice = productVariant.getPriceWithDiscount();
    }
}
