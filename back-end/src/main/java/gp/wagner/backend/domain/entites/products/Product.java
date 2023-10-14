package gp.wagner.backend.domain.entites.products;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.eav.AttributeValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
//import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

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
    //@FullTextField
    private String name;

    //Описание товара
    @Column(name = "description")
    //@FullTextField
    private String description;

    //Связующие свойство категории товара (Многие товары к 1 категории)
    @JoinColumn(name = "category_id")
    @ManyToOne()
    private Category category;

    //Связующие свойство производителя товара (Многие товаров к 1 производителю)
    @JoinColumn(name = "producer_id")
    @ManyToOne()
    private Producer producer;

    //Наличие товара
    @Column(name = "is_available")
    private Boolean isAvailable;

    //Флаг вывода товара
    @Column(name = "show_product")
    private Boolean showProduct;

    //Характеристики товара (Многие характеристики к 1 товару)
    @OneToMany(mappedBy = "product")
    private List<AttributeValue> attributeValues = new ArrayList<>();

    //Варианты исполнения товара

    @OneToMany(mappedBy = "product")
    private List<ProductVariant> productVariants = new ArrayList<>();
}
