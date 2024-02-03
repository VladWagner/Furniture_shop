package gp.wagner.backend.domain.entites.categories;

import gp.wagner.backend.domain.entites.eav.ProductAttribute;
import gp.wagner.backend.domain.entites.products.Product;
import jakarta.annotation.Nullable;
import lombok.*;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.List;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private RepeatingCategory repeatingCategory;

    public String getName(){
        return name != null ? name : repeatingCategory.getName();
    }

    //Связующие свойство родительской категории
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parentCategory;

    //Товары, которые принадлежат данной категории
    @OneToMany(mappedBy = "category")
    @BatchSize(size = 20)
    private List<Product> products;

    //Атрибуты под конкретную категорию - названия характеристик
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "attributes_categories",
            joinColumns = {@JoinColumn(name = "category_id")},
            inverseJoinColumns = {@JoinColumn(name = "attribute_id")})
    private Set<ProductAttribute> productAttributes = new HashSet<>();

    // Флаг вывода товаров категории
    @Column(name = "is_shown")
    private Boolean isShown;

    public Category(Long id, @NonNull String name, RepeatingCategory repeatingCategory, Category parentCategory) {
        this.id = id;
        this.name = name;
        this.repeatingCategory = repeatingCategory;
        this.parentCategory = parentCategory;
    }
}

