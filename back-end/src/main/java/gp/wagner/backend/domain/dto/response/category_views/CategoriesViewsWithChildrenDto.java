package gp.wagner.backend.domain.dto.response.category_views;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.visits.CategoryViews;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriesViewsWithChildrenDto {

    //Наименование категории
    private String categoryName;
    @Min(1)
    //id категории
    private long categoryId;

    //Общее кол-во просмотров всех дочерних категорий или текущей категории
    private int generalViewsAmount;

    //Список посмотров дочерних категорий
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
