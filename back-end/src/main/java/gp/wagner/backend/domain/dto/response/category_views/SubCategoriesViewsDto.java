package gp.wagner.backend.domain.dto.response.category_views;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.visits.CategoryViews;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubCategoriesViewsDto {

    /*//Наименование категории
    private String categoryName;
    @Min(1)
    //id категории
    private long categoryId;

    //Родительская категория
    @Nullable
    private Long parentCategoryId;*/

    @Min(1)
    private long visitorId;

    //Кол-во просмотров категории конкретным пользователем
    private int count;

    public SubCategoriesViewsDto(CategoryViews categoryViews) {

        /*Category viewedCategory = categoryViews.getCategory();

        this.categoryName = viewedCategory.getName();
        this.categoryId = viewedCategory.getId();
        this.parentCategoryId = viewedCategory.getParentCategory().getId();*/
        this.visitorId = categoryViews.getVisitor().getId();
        this.count = categoryViews.getCount();
    }
}
