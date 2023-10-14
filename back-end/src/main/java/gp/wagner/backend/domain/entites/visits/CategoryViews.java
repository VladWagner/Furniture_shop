package gp.wagner.backend.domain.entites.visits;

import gp.wagner.backend.domain.entites.categories.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

//Количество просмотров товара
@Entity
@Table(name = "categories_views")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryViews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Связующее свойство посетителя (Многие просмотры категорий для 1 пользователя,
    // ведь пользователь может просматривать разные категории по нескольку раз)
    @ManyToOne
    @JoinColumn(name = "visitor_id")
    private Visitor visitor;

    //Связующее свойство категории (Многие просмотры от разных пользователей к 1 категории)
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    //Общее количество
    @Column(name = "count")
    private int count;


}
