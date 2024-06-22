package gp.wagner.backend.domain.entities.categories;

import gp.wagner.backend.domain.entities.eav.ProductAttribute;
import gp.wagner.backend.domain.entities.products.Product;
import lombok.*;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.*;

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
    /*@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "attributes_categories",
            joinColumns = {@JoinColumn(name = "category_id")},
            inverseJoinColumns = {@JoinColumn(name = "attribute_id")})*/
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "categories")
    private List<ProductAttribute> productAttributes = new ArrayList<>();

    // Флаг вывода товаров категории
    @Column(name = "is_shown")
    private Boolean isShown = true;

    // Изображение для категории
    @Column(name = "image")
    private String image;

    public String getImage() {

        if (image == null && repeatingCategory != null && repeatingCategory.getImage() != null)
            return repeatingCategory.getImage();

        return image;
    }

    public Category(Long id, String name, RepeatingCategory repeatingCategory, Category parentCategory) {
        this.id = id;
        this.name = name;
        this.repeatingCategory = repeatingCategory;
        this.parentCategory = parentCategory;
    }
    public Category(Long id, String name, RepeatingCategory repeatingCategory, Category parentCategory, String categoryImg) {
        this(id, name, repeatingCategory, parentCategory);
        this.image = categoryImg;
    }

    public Category(Long id, String name, RepeatingCategory repeatingCategory, Category parentCategory, boolean isShown) {
        this(id, name, repeatingCategory, parentCategory);
        this.isShown = isShown;
    }

}


