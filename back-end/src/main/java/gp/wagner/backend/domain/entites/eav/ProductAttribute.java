package gp.wagner.backend.domain.entites.eav;

import gp.wagner.backend.domain.entites.categories.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    //Атрибуты под конкретную категорию
    /*@ManyToMany(mappedBy = "productAttributes",fetch = FetchType.LAZY)
    private List<Category> categories = new ArrayList<>();*/
}
