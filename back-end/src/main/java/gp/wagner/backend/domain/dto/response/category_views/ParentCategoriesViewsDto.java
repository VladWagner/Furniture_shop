package gp.wagner.backend.domain.dto.response.category_views;

import gp.wagner.backend.domain.entities.categories.Category;
import gp.wagner.backend.domain.entities.visits.CategoryViews;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParentCategoriesViewsDto {

    //Наименование категории
    private String categoryName;
    @Min(1)
    //id категории
    private long categoryId;

    //Общее кол-во просмотров всех дочерних категорий
    private int generalViewsAmount;

    public ParentCategoriesViewsDto(CategoryViews categoryViews) {

        Category viewedCategory = categoryViews.getCategory();
        this.categoryName = viewedCategory.getName();
        this.categoryId = viewedCategory.getId();
        this.generalViewsAmount = categoryViews.getCount();
    }
}
