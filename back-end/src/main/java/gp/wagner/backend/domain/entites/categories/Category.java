package gp.wagner.backend.domain.entites.categories;

import gp.wagner.backend.domain.entites.eav.ProductAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

//Категории
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Наименование категории
    @Column(name = "category_name")
    private String name;

    //Связующее свойство повторяющихся категорий - в основном для подкатегорий, которые для разных категорий могут быть одним и тем же.
    //Например, категория: спальни и в ней подкатегория шкафы или категория: гостиные и в ней так же категория шкафы.
    @ManyToOne
    @JoinColumn(name = "subcategory_id")
    private RepeatingCategory repeatingCategory;

    public String getName(){
        return name != null ? name : repeatingCategory.getName();
    }

    //Связующие свойство родительской категории
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parentCategory;

    //Атрибуты под конкретную категорию - названия характеристик
    //Проблемы начинаются здесь - LazyInitializationException
    /*@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "attributes_categories",
            joinColumns = {@JoinColumn(name = "category_id")},
            inverseJoinColumns = {@JoinColumn(name = "attribute_id")})
    private Set<ProductAttribute> productAttributes = new HashSet<>();*/

}

