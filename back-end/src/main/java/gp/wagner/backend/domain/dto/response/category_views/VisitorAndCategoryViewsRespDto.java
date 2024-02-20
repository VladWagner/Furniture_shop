package gp.wagner.backend.domain.dto.response.category_views;

import com.fasterxml.jackson.annotation.JsonProperty;
import gp.wagner.backend.domain.dto.response.VisitorRespDto;
import gp.wagner.backend.domain.entites.categories.Category;
import gp.wagner.backend.domain.entites.visits.Visitor;
import gp.wagner.backend.middleware.Services;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//Посетители и категории, которые они смотрели
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VisitorAndCategoryViewsRespDto {

    // Посетитель
    private VisitorRespDto visitorDto;

    // Список просмотренных им категорий
    @JsonProperty("categories_views")
    private List<CategoriesViewsWithChildrenDto> categoriesViewsDtos;

    // Кол-во просмотренных категорий
    @JsonProperty("categories_viewed")
    private int categoriesViewed;

    // Сумма просмотров всех категорий
    @JsonProperty("views_sum")
    private int viewsSum;

    public VisitorAndCategoryViewsRespDto(VisitorRespDto visitorDto, List<CategoriesViewsWithChildrenDto> categoriesViewsDtos) {
        this.visitorDto = visitorDto;
        this.categoriesViewsDtos = categoriesViewsDtos;
        categoriesViewed = categoriesViewsDtos.size();
        this.viewsSum = categoriesViewsDtos.stream()
                .mapToInt(CategoriesViewsWithChildrenDto::getGeneralViewsAmount)
                .sum();
    }

    public VisitorAndCategoryViewsRespDto(Tuple tuple) {

        Visitor visitor = Services.visitorsService.getById(tuple.get(0, Long.class));
        this.visitorDto = new VisitorRespDto(visitor);

        // Получить просмотры категорий для заданного посетителя
        this.categoriesViewsDtos = visitor.getCategoriesViewsList().stream().map(cv -> {
            Category category = cv.getCategory();
            return new CategoriesViewsWithChildrenDto(category.getName(), category.getId(), cv.getCount(), null);
        }).collect(Collectors.toCollection(ArrayList::new));

        this.categoriesViewed = tuple.get(1, Long.class).intValue();
        this.viewsSum = tuple.get(2, Integer.class);
    }

    public VisitorAndCategoryViewsRespDto(Tuple tuple, Map<Long,Visitor> foundVisitors) {

        Visitor visitor = foundVisitors.get(tuple.get(0, Long.class));
        this.visitorDto = new VisitorRespDto(visitor);

        // Получить просмотры категорий для заданного посетителя
        this.categoriesViewsDtos = visitor.getCategoriesViewsList().stream().map(cv -> {
            Category category = cv.getCategory();
            return new CategoriesViewsWithChildrenDto(category.getName(), category.getId(), cv.getCount(), null);
        }).collect(Collectors.toCollection(ArrayList::new));

        this.categoriesViewed = tuple.get(1, Long.class).intValue();
        this.viewsSum = tuple.get(2, Integer.class);
    }
}
