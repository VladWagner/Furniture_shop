package gp.wagner.backend.domain.dto.response.categories;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.middleware.Services;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private long id;

    @JsonProperty("category_name")
    private String categoryName;

    //Id родительской категории, если такая имеется
    @JsonProperty("parent_category_id")
    private int parentCategoryId;

    //Количество товаров в данной категории
    @JsonProperty("products_amount")
    private int productsAmount;

    public CategoryDto(Category category, int productsAmount) {
        this.id = category.getId();
        this.categoryName = category.getName();
        this.parentCategoryId = category.getParentCategory() != null ? category.getParentCategory().getId().intValue() : 0;
        this.productsAmount = productsAmount;
    }

    //Фабричный метод
    public static CategoryDto factory(Category category){

        return new CategoryDto(category, Services.productsService.countByCategory(category.getId()));
    }

    //  Перегруженный фабричный метод
    public static void factory(Category category, Map<Long, CategoryDto> categoryDtoMap){

        long categoryId = category.getRepeatingCategory() != null ? category.getRepeatingCategory().getId() * -1 : category.getId();

        // Если такая категория не была задана в ассоциативную коллекцию
        if (!categoryDtoMap.containsKey(categoryId)) {

            CategoryDto dto = new CategoryDto(category, Services.productsService.countByCategory(categoryId));

            // Если полученный id < 0, тогда это id повторяющейся категории (по договоренности для идентификации он должен быть отрицательным)
            if (categoryId < 0) {
                dto.setId(categoryId);
                dto.setParentCategoryId(0);
            }

            categoryDtoMap.put(categoryId, dto);
        }
    }
}
