package gp.wagner.backend.domain.dto.response.categories;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.categories.RepeatingCategory;
import gp.wagner.backend.middleware.Services;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// DTO для выборки категорий с вложенным списком дочерних категорий
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBreadcrumbsDto {

    // Id категории (для RepeatingCategory будет задано отрицательное значение id)
    @JsonProperty("category_id")
    private Long categoryId;


    @JsonProperty("category_name")
    private String categoryName;

    // Дочерние категории
    @JsonProperty("child_bread_crumb")
    private CategoryBreadcrumbsDto childBreadCrumb;

    public CategoryBreadcrumbsDto(Category category) {
        this.categoryId = category.getId();
        this.categoryName = category.getName();
    }
    public CategoryBreadcrumbsDto(RepeatingCategory repeatingCategory) {
        this.categoryId = repeatingCategory.getId()*-1L;
        this.categoryName = repeatingCategory.getName();
    }
}