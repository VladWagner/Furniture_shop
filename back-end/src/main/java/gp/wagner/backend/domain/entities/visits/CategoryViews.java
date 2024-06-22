package gp.wagner.backend.domain.entities.visits;

import gp.wagner.backend.domain.entities.categories.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDateTime;

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

    // Связующее свойство посетителя (Многие просмотры категории для 1 посетителя,
    // ведь посетителя может просматривать разные категории по нескольку раз)
    @ManyToOne
    @JoinColumn(name = "visitor_id")
    private Visitor visitor;

    // Связующее свойство категории (Многие просмотры от разных пользователей к 1 категории)
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // Общее количество
    @Column(name = "count")
    private int count;

    // Дата обновления
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CategoryViews(Long id, Visitor visitor, Category category, int count) {
        this.id = id;
        this.visitor = visitor;
        this.category = category;
        this.count = count;
    }

    // Проверка прошло или >= определённого кол-ва часов с момента последнего зачёта просмотра категории
    public boolean goneMoreThan(long hours){

        LocalDateTime now = LocalDateTime.now();

        Duration duration = Duration.between(updatedAt, now);
        return duration.toHours() >= hours;
    }

}