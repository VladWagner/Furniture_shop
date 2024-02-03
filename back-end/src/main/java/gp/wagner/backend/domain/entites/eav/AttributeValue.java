package gp.wagner.backend.domain.entites.eav;

import gp.wagner.backend.domain.entites.products.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

// Значения атрибутов - значения характеристик
@Entity
@Table(name = "attributes_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связующие свойство атрибута (Многие значения атрибута к 1 атрибуту)
    @ManyToOne()
    @JoinColumn(name = "attribute_id")
    private ProductAttribute attribute;

    // Связующие свойство товара (Многие значения различных атрибутов (характеристик) к 1 товару)
    @ManyToOne()
    @JoinColumn(name = "product_id")
    private Product product;

    // Строковое значение
    @Column(name = "txt_values")
    private String strValue = null;

    // Целочисленное значение
    @Column(name = "int_value")
    private Integer intValue = null;

    // Значение с плавающей запятой
    @Column(name = "float_value")
    private Float floatValue = null;

    // Значение double
    @Column(name = "double_value")
    private Double doubleValue = null;

    // Значение bool
    @Column(name = "bool_value")
    private Boolean boolValue = null;

    // Значение даты
    @Column(name = "date_value")
    private Date dateValue = null;

}
