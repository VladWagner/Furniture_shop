package gp.wagner.backend.domain.dto.response.categories;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.middleware.Services;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

// DTO для выборки категорий с вложенным списком дочерних категорий
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDtoWithChildren {

    private long id;

    @JsonProperty("category_name")
    private String categoryName;

    // Id родительской категории, если такая имеется
    @JsonProperty("parent_category_id")
    private Long parentCategoryId;

    // Дочерние категории
    @JsonProperty("child_categories")
    private List<CategoryDtoWithChildren> childCategories;

    //Количество товаров в данной категории
    @JsonProperty("products_amount")
    private Integer productsAmount;

    public CategoryDtoWithChildren(Category category, Integer productsAmount) {
        this.id = category.getId();
        this.categoryName = category.getName();
        this.parentCategoryId = category.getParentCategory() != null ? category.getParentCategory().getId() : 0;
        this.productsAmount = productsAmount;
    }

    //Фабричный метод
    public static CategoryDtoWithChildren factory(Category category){

        return new CategoryDtoWithChildren(category, Services.productsService.countByCategory(category.getId()));
    }

}