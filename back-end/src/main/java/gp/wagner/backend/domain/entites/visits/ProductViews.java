package gp.wagner.backend.domain.entites.visits;

import gp.wagner.backend.domain.entites.products.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

//Количество просмотров товара
@Entity
@Table(name = "products_views")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductViews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Связующее свойство посетителя (Многие просторы различных товаров к пользователю)
    @ManyToOne
    @JoinColumn(name = "visitor_id")
    private Visitor visitor;

    //Связующее свойство товара (Многие просмотры от различных пользователей к 1 товару)
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    //Общее количество
    @Column(name = "count")
    private int count;


}
