package gp.wagner.backend.domain.dto.response.product_attributes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.eav.ProductAttribute;
import gp.wagner.backend.domain.entites.products.Producer;
import jakarta.annotation.Nullable;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// DTO для возврата атрибута товара
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeRespDto {

    private long id;

    // Название атрибута
    @JsonProperty("attribute_name")
    private String attributeName;

    // Приоритет
    private Float priority;

    // Флаг вывода
    @JsonProperty("is_shown")
    private Boolean isShown;

    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class CategoryInProductAttribute{

        @JsonProperty("category_id")
        long categoryId;
        @JsonProperty("category_name")
        String categoryName;
        @JsonProperty("parent_category")
        @Nullable
        String parentCategory;

        public CategoryInProductAttribute(Category category) {
            this.categoryId = category.getId();
            this.categoryName = category.getName();
            this.parentCategory = category.getParentCategory().getName();
        }
    }

    // Список категорий к которым принадлежит атрибут
    private List<CategoryInProductAttribute> categories;

    public ProductAttributeRespDto(ProductAttribute pa) {
        this.id = pa.getId();
        this.attributeName = pa.getAttributeName();
        this.priority = pa.getPriority();
        this.isShown = pa.getIsShown();
        this.categories = pa.getCategories()
                .stream()
                .map(CategoryInProductAttribute::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
