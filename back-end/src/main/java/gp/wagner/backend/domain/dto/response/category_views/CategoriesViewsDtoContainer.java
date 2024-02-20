package gp.wagner.backend.domain.dto.response.category_views;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.visits.CategoryViews;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriesViewsDtoContainer {

    //Название категории
    private String categoryName;

    //Id категории
    private long categoryId;

    //Родительская категория
    @Nullable
    private Long parentCategoryId;

    //Общее кол-во просмотров в категории
    private int generalAmountOfViews;

    //Просмотры дочерних категорий
    private List<SubCategoriesViewsDto> subCategoriesViewsDtos;


    public void setSubCategoriesViewsDtos(List<CategoryViews> categoryViewsList){
        subCategoriesViewsDtos = categoryViewsList.stream()
                .map(SubCategoriesViewsDto::new)
                .toList();

        //Подсчитать общую сумму просмотров
        generalAmountOfViews = categoryViewsList.stream().mapToInt(CategoryViews::getCount).sum();
    }

    //Просмотры родительской категории
    //private ParentCategoriesViewsDto parentCategoriesViewsDto;

    public void setParentViewsCount(CategoryViews categoryViews) {
        //this.parentCategoriesViewsDto = new ParentCategoriesViewsDto(categoryViews);
        generalAmountOfViews = categoryViews.getCount();
    }

    public CategoriesViewsDtoContainer(String categoryName, long categoryId, Long parentCategoryId) {
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.parentCategoryId = parentCategoryId;
    }

    public CategoriesViewsDtoContainer(Category category) {
        this.categoryName = category.getName() != null ? category.getName() : category.getRepeatingCategory().getName();
        this.categoryId = category.getId();

        Category parentCategory = category.getParentCategory();

        this.parentCategoryId = parentCategory != null ? parentCategory.getId() : null;
    }
}
