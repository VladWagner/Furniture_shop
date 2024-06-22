package gp.wagner.backend.domain.entities.products;

import gp.wagner.backend.domain.entities.categories.Category;
import gp.wagner.backend.domain.entities.eav.AttributeValue;
import gp.wagner.backend.domain.entities.ratings.RatingStatistics;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

//Товары - довольно составная сущность, ссылается на категории, характеристики, производителей
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@Indexed(index = "idx_product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Наименование товара
    @Column(name = "product_name")
    //@FullTextField(analyzer = "product_analyzer")
    private String name;

    //Описание товара
    @Column(name = "description")
    //@FullTextField(analyzer = "product_analyzer")
    private String description;

    //Связующие свойство категории товара (Многие товары к 1 категории)
    @JoinColumn(name = "category_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @BatchSize(size = 256)
    private Category category;

    //Связующие свойство производителя товара (Многие товаров к 1 производителю)
    @JoinColumn(name = "producer_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @BatchSize(size = 256)
    private Producer producer;

    //Наличие товара
    @Column(name = "is_available")
    private Boolean isAvailable;

    // Флаг удалён ли товар
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    //Флаг вывода товара
    //false == 0
    @Column(name = "show_product")
    private Boolean showProduct;

    // Характеристики товара (Многие характеристики к 1 товару)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    @BatchSize(size = 256)
    private List<AttributeValue> attributeValues = new ArrayList<>();

    // Варианты исполнения товара

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    @BatchSize(size = 256)
    private List<ProductVariant> productVariants = new ArrayList<>();

    // Статистика по оценкам
    @OneToOne(mappedBy = "product")
    private RatingStatistics ratingStatistics;

}
