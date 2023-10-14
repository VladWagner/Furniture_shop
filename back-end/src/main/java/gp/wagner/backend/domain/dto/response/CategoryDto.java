package gp.wagner.backend.domain.dto.response;

import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.middleware.Services;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private int id;

    private String categoryName;

    //Id родительской категории, если такая имеется
    private int parentCategoryId;

    //Количество товаров в данной категории
    private int productsAmount;

    public CategoryDto(Category category, int productsAmount) {
        this.id = category.getId().intValue();
        this.categoryName = category.getName();
        this.parentCategoryId = category.getParentCategory() != null ? category.getParentCategory().getId().intValue() : 0;
        this.productsAmount = productsAmount;
    }

    //Фабричный метод
    public static CategoryDto factory(Category category){

        return new CategoryDto(category, Services.productsService.countByCategory(category.getId()));
    }
}
