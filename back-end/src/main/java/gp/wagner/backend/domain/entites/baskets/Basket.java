package gp.wagner.backend.domain.entites.baskets;


import gp.wagner.backend.domain.entites.orders.OrderAndProductVariant;
import gp.wagner.backend.domain.entites.users.User;
import jakarta.persistence.*;
import gp.wagner.backend.domain.entites.products.ProductVariant;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.Date;
import java.util.List;

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

    //Дата и время добавления товара в корзину
    @Column(name = "added_date")
    private Date addedDate;

    //Связующее свойство пользователя
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    //Варианты товаров для данной корзины
    @OneToMany(mappedBy = "basket")
    @BatchSize(size = 256)
    private List<BasketAndProductVariant> basketAndPVList;

    // Итоговая сумма заказа
    @Column(name = "sum")
    private int sum;

}
