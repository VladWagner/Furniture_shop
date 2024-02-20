package gp.wagner.backend.domain.entites.eav;

import gp.wagner.backend.domain.entites.categories.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

// Атрибуты (характеристики товаров)
@Entity
@Table(name = "products_attributes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Наименование атрибута
    @Column(name = "attr_name")
    private String attributeName;

    // Приоритет вывода атрибута в фильтре
    @Column(name = "priority")
    private Float priority;

    // Флаг показа атрибута
    @Column(name = "is_shown")
    private Boolean isShown;


    // Атрибуты под категории
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "attributes_categories",
            joinColumns = {@JoinColumn(name = "attribute_id")},
            inverseJoinColumns = {@JoinColumn(name = "category_id")})
    private List<Category> categories = new ArrayList<>();

    public ProductAttribute(Long id, String attributeName, Float priority, Boolean isShown) {
        this.id = id;
        this.attributeName = attributeName;
        this.priority = priority;
        this.isShown = isShown;
    }
}
