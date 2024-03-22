package gp.wagner.backend.domain.dto.response.category_views;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.visits.CategoryViews;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriesViewsWithChildrenDto {

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("category_id")
    private long categoryId;

    // Общее кол-во просмотров текущей категории или всех дочерних категорий + текущей (родительской категории)
    @JsonProperty("general_views_amount")
    private int generalViewsAmount;

    // Список просмотров дочерних категорий
    @JsonProperty("child_categories_views")
    public List<CategoriesViewsWithChildrenDto> childCategories;

    public CategoriesViewsWithChildrenDto(CategoryViews categoryViews) {
        Category viewedCategory = categoryViews.getCategory();
        this.categoryName = viewedCategory.getName();
        this.categoryId = viewedCategory.getId();
        this.generalViewsAmount = categoryViews.getCount();

        this.childCategories = new ArrayList<>();
    }

    public CategoriesViewsWithChildrenDto(Category category) {

        this.categoryName = category.getName();
        this.categoryId = category.getId();
        this.generalViewsAmount = 0;
        this.childCategories = new ArrayList<>();
    }
}
