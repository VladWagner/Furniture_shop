package gp.wagner.backend.domain.entites.eav;

import gp.wagner.backend.domain.entites.categories.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Атрибуты (характеристики товаров)
//Данная сущность "хранит" все наименования атрибутов + по идее должна соединятся с категориями (м к м),
//чтобы было проще находить доступные характеристики для определённой категории или подкатегории
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


    //Атрибуты под конкретную категорию
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "attributes_categories",
            joinColumns = {@JoinColumn(name = "attribute_id")},
            inverseJoinColumns = {@JoinColumn(name = "category_id")})
    private List<Category> productAttributes = new ArrayList<>();
}
