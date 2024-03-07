package gp.wagner.backend.domain.entites.orders;


import gp.wagner.backend.domain.entites.products.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.Date;
import java.util.List;

//Заказ
//Выполняется по определённому варианту товара (Много заказов к 1 варианту товара)
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Дата и время заказа
    @Column(name = "order_date")
    private Date orderDate;

    //Код заказа
    @Column(name = "code")
    private long code;

    // Описание заказа
    @Column(name = "description")
    private String description;

    //Связующее свойство состояния заказа (Многие заказы к 1 статусу)
    @ManyToOne
    @JoinColumn(name = "order_state_id")
    private OrderState orderState;


    // Связующее свойство способа оплаты заказа
    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    //Связующее свойство покупателя (снова многие заказы к 1 пользователю)
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    //Варианты товаров для данного заказа
    @OneToMany(mappedBy = "order")
    @BatchSize(size = 256)
    private List<OrderAndProductVariant> orderAndPVList;

    // Товары для данного заказа
    /*@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "orders_products_variants",
            joinColumns = {@JoinColumn(name = "order_id")},
            inverseJoinColumns = {@JoinColumn(name = "product_variant_id")}
    )
    private List<ProductVariant> productVariants;*/

    // Итоговая сумма заказа
    @Column(name = "sum")
    private int sum;

    // Общее кол-во заказов сумма заказа
    @Column(name = "general_products_amount")
    private int generalProductsAmount;

}
