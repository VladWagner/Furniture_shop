package gp.wagner.backend.domain.entites.basket;


import gp.wagner.backend.domain.entites.users.User;
import jakarta.persistence.*;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;

//Корзина для авторизированных пользователей
@Entity
@Table(name = "baskets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Basket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Связующее свойство варианта товара
    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    //Количество заказываемых вариантов товара
    @Column(name = "products_amount")
    private int productsAmount;

    //Дата и время добавления товара в корзину
    @Column(name = "added_date")
    private Date addedDate;

    //Связующее свойство пользователя
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
